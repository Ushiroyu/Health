export interface Article {
  articleId: number;
  title: string;
  content: string;
  category?: string;
  authorId?: number;
  publishDate?: string;
  status?: string;
  reviewerId?: number;
  reviewedAt?: string;
  rejectReason?: string;
}
