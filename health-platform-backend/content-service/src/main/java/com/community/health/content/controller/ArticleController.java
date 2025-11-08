package com.community.health.content.controller;

import com.community.health.common.api.ApiResponse;
import com.community.health.content.entity.Article;
import com.community.health.content.repo.ArticleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/content/article")
public class ArticleController {
  private final ArticleRepository repo;
  private final com.community.health.content.repo.AuditLogRepository auditRepo;
  private final Optional<com.community.health.content.mq.ContentEventPublisher> eventPublisher;
  public ArticleController(ArticleRepository r,
                           com.community.health.content.repo.AuditLogRepository a,
                           Optional<com.community.health.content.mq.ContentEventPublisher> publisher){
    this.repo = r; this.auditRepo = a; this.eventPublisher = publisher;
  }

  @PostMapping("/save")
  public ApiResponse<?> save(@RequestBody Article a){ return ApiResponse.ok(repo.save(a)); }

  @GetMapping("/list")
  public ApiResponse<?> list(){ return ApiResponse.ok(repo.findAll(org.springframework.data.domain.PageRequest.of(0, 100, org.springframework.data.domain.Sort.by("publishDate").descending()))); }

  @GetMapping("/{id}")
  public ApiResponse<?> detail(@PathVariable("id") Long id){
    return repo.findById(id).<ApiResponse<?>>map(ApiResponse::ok)
      .orElseGet(() -> new ApiResponse<>(com.community.health.common.exception.ErrorCodes.NOT_FOUND, "文章不存在", null));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<?> delete(@PathVariable("id") Long id){
    if (!repo.existsById(id)) return new ApiResponse<>(com.community.health.common.exception.ErrorCodes.NOT_FOUND, "文章不存在", null);
    repo.deleteById(id); return ApiResponse.ok();
  }

  @GetMapping("/search")
  public ApiResponse<?> search(@RequestParam(value = "q", required = false) String q,
                               @RequestParam(value = "category", required = false) String category,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "20") int size) {
    var p = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("publishDate").descending());
    if (category != null && !category.isBlank()) return ApiResponse.ok(repo.findByCategoryOrderByPublishDateDesc(category, p));
    if (q == null || q.isBlank()) return ApiResponse.ok(repo.findAll(p));
    return ApiResponse.ok(repo.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(q, q, p));
  }

  // admin endpoints
  @PostMapping("/admin/article")
  public ApiResponse<?> create(@RequestHeader("X-User-Id") Long uid, @RequestBody Article a){ a.setAuthorId(uid); a.setStatus("DRAFT"); return ApiResponse.ok(repo.save(a)); }
  @PutMapping("/admin/article/{id}")
  public ApiResponse<?> update(@PathVariable("id") Long id, @RequestBody Article a){ a.setArticleId(id); return ApiResponse.ok(repo.save(a)); }
  @PostMapping("/admin/article/{id}/submit")
  public ApiResponse<?> submit(@RequestHeader("X-User-Id") Long uid, @PathVariable("id") Long id){
    return repo.findById(id).map(x -> { x.setStatus("PENDING"); audit(uid, "SUBMIT", id); return ApiResponse.ok(repo.save(x)); })
      .orElseGet(() -> new ApiResponse<>(com.community.health.common.exception.ErrorCodes.NOT_FOUND, "文章不存在", null));
  }
  @PostMapping("/admin/article/{id}/approve")
  public ApiResponse<?> approve(@RequestHeader("X-User-Id") Long reviewerId, @RequestHeader("X-User-Role") String role, @PathVariable("id") Long id){
    if (role==null || !role.equalsIgnoreCase("ADMIN")) throw new com.community.health.common.exception.BusinessException(403, "仅管理员可审核");
    return repo.findById(id).map(x -> { x.setStatus("APPROVED"); x.setReviewerId(reviewerId); x.setReviewedAt(java.time.LocalDateTime.now()); audit(reviewerId, "APPROVE", id); return ApiResponse.ok(repo.save(x)); })
      .orElseGet(() -> new ApiResponse<>(com.community.health.common.exception.ErrorCodes.NOT_FOUND, "文章不存在", null));
  }
  @PostMapping("/admin/article/{id}/reject")
  public ApiResponse<?> reject(@RequestHeader("X-User-Id") Long reviewerId, @RequestHeader("X-User-Role") String role, @PathVariable("id") Long id, @RequestParam("reason") String reason){
    if (role==null || !role.equalsIgnoreCase("ADMIN")) throw new com.community.health.common.exception.BusinessException(403, "仅管理员可审核");
    return repo.findById(id).map(x -> { x.setStatus("REJECTED"); x.setReviewerId(reviewerId); x.setReviewedAt(java.time.LocalDateTime.now()); x.setRejectReason(reason); audit(reviewerId, "REJECT", id+":"+reason); return ApiResponse.ok(repo.save(x)); })
      .orElseGet(() -> new ApiResponse<>(com.community.health.common.exception.ErrorCodes.NOT_FOUND, "文章不存在", null));
  }
  @PostMapping("/admin/article/{id}/publish")
  public ApiResponse<?> publish(@PathVariable("id") Long id){
    return repo.findById(id).map(x -> {
      if (!"APPROVED".equals(x.getStatus())) throw new com.community.health.common.exception.BusinessException(com.community.health.common.exception.ErrorCodes.CONTENT_NOT_APPROVED, "未审核通过不可发布");
      x.setPublishDate(java.time.LocalDateTime.now());
      audit(null, "PUBLISH", id);
      var saved = repo.save(x);
      eventPublisher.ifPresent(p -> p.publishArticle(saved));
      return ApiResponse.ok(saved);
    }).orElseGet(() -> new ApiResponse<>(com.community.health.common.exception.ErrorCodes.NOT_FOUND, "文章不存在", null));
  }
  @PostMapping("/admin/article/{id}/unpublish")
  public ApiResponse<?> unpublish(@PathVariable("id") Long id){
    return repo.findById(id).map(x -> {
      x.setPublishDate(null);
      audit(null, "UNPUBLISH", id);
      var saved = repo.save(x);
      eventPublisher.ifPresent(p -> p.publishArticle(saved));
      return ApiResponse.ok(saved);
    }).orElseGet(() -> new ApiResponse<>(com.community.health.common.exception.ErrorCodes.NOT_FOUND, "文章不存在", null));
  }

  private void audit(Long uid, String action, Object details){
    auditRepo.save(com.community.health.content.entity.AuditLog.builder().ts(java.time.LocalDateTime.now()).userId(uid).action(action).targetType("ARTICLE").targetId(null).details(details==null?null:String.valueOf(details)).build());
  }
}
