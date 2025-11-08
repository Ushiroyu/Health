<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <el-input v-model="filters.q" placeholder="标题关键词" clearable style="width: 200px" />
        <el-select v-model="filters.category" placeholder="分类" clearable style="width: 160px">
          <el-option label="健康生活" value="健康生活" />
          <el-option label="疾病预防" value="疾病预防" />
          <el-option label="健康知识" value="健康知识" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </div>
    </template>
    <el-timeline>
      <el-timeline-item
        v-for="article in articles?.content ?? []"
        :key="article.articleId"
        :timestamp="format(article.publishDate)"
        @click="view(article.articleId)"
      >
        <div class="article-item">
          <h3>{{ article.title }}</h3>
          <p>{{ article.content?.slice(0, 120) }}...</p>
        </div>
      </el-timeline-item>
    </el-timeline>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { PageResult } from '@/types/common';
import type { Article } from '@/types/content';
import { fetchArticles } from '@/services/content';
import { formatDate } from '@/utils/format';

const router = useRouter();
const articles = ref<PageResult<Article>>();

const filters = reactive({
  q: '',
  category: ''
});

const load = async () => {
  articles.value = await fetchArticles({
    q: filters.q || undefined,
    category: filters.category || undefined
  });
};

onMounted(load);

const format = (value?: string) => formatDate(value, '');
const view = (id: number) => router.push({ name: 'content-article-detail', params: { id } });
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.article-item {
  cursor: pointer;
}
</style>
