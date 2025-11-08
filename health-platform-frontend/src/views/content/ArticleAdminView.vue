<template>
  <div class="admin">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>文章管理</span>
          <el-button type="primary" @click="openDialog()">新建文章</el-button>
        </div>
      </template>
      <el-table :data="list">
        <el-table-column prop="articleId" label="编号" width="100" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="publishDate" label="发布时间" width="180" />
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button link @click="openDialog(row)">编辑</el-button>
            <el-button link type="primary" @click="submit(row)">提交审核</el-button>
            <el-button link type="success" @click="review(row, 'approve')">审核通过</el-button>
            <el-button link type="danger" @click="review(row, 'reject')">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="current?.articleId ? '编辑文章' : '创建文章'" width="600px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category">
            <el-option label="健康生活" value="健康生活" />
            <el-option label="疾病预防" value="疾病预防" />
            <el-option label="健康知识" value="健康知识" />
          </el-select>
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.content" type="textarea" :rows="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { Article } from '@/types/content';
import { fetchArticles, saveArticle, submitArticle, reviewArticle } from '@/services/content';

const list = ref<Article[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);
const current = ref<Article | null>(null);

const form = reactive<Partial<Article>>({
  title: '',
  category: '',
  content: ''
});

const load = async () => {
  const res = await fetchArticles({ size: 100 });
  list.value = res.content;
};

onMounted(load);

const openDialog = (article?: Article) => {
  current.value = article ?? null;
  Object.assign(form, {
    articleId: article?.articleId,
    title: article?.title ?? '',
    category: article?.category ?? '',
    content: article?.content ?? ''
  });
  dialogVisible.value = true;
};

const save = async () => {
  saving.value = true;
  try {
    await saveArticle(form);
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    load();
  } finally {
    saving.value = false;
  }
};

const submit = async (article: Article) => {
  await submitArticle(article.articleId);
  ElMessage.success('已提交审核');
  load();
};

const review = async (article: Article, action: 'approve' | 'reject') => {
  if (action === 'reject') {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    });
    await reviewArticle(article.articleId, 'reject', value);
  } else {
    await reviewArticle(article.articleId, 'approve');
  }
  ElMessage.success('已更新状态');
  load();
};
</script>

<style scoped>
.admin {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
