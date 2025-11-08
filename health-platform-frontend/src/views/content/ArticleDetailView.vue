<template>
  <el-card shadow="never" v-if="article">
    <template #header>
      <div class="header">
        <span>{{ article.title }}</span>
        <span class="time">{{ format(article.publishDate) }}</span>
      </div>
    </template>
    <div class="content" v-html="article.content" />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import type { Article } from '@/types/content';
import { fetchArticleDetail } from '@/services/content';
import { formatDate } from '@/utils/format';

const route = useRoute();
const article = ref<Article>();

const load = async () => {
  const id = Number(route.params.id);
  if (!Number.isFinite(id)) return;
  article.value = await fetchArticleDetail(id);
};

onMounted(() => {
  load();
});

const format = (value?: string) => formatDate(value);
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.time {
  color: #6b7280;
  font-size: 12px;
}
.content {
  line-height: 1.7;
}
</style>
