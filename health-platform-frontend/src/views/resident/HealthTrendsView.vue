<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <el-radio-group v-model="query.type">
          <el-radio-button label="BP">血压</el-radio-button>
          <el-radio-button label="BG">血糖</el-radio-button>
          <el-radio-button label="WEIGHT">体重</el-radio-button>
        </el-radio-group>
        <el-date-picker
          v-model="range"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="load"
        />
      </div>
    </template>
    <div ref="chartRef" class="chart"></div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';
import { fetchTrend } from '@/services/resident';

const query = ref({
  type: 'BP'
});

const range = ref<[string, string] | null>(null);
const chartRef = ref<HTMLDivElement>();
let chart: echarts.ECharts | null = null;

const initRange = () => {
  const end = new Date();
  const start = new Date();
  start.setDate(end.getDate() - 30);
  range.value = [start.toISOString().slice(0, 10), end.toISOString().slice(0, 10)];
};

const initChart = () => {
  if (!chartRef.value) return;
  chart = echarts.init(chartRef.value);
};

const load = async () => {
  if (!range.value) return;
  const data = await fetchTrend({
    type: query.value.type,
    from: range.value[0],
    to: range.value[1]
  });
  const dates = data.series.map((item) => item.date);
  const values = data.series.map((item) => Number(item.value.toFixed(2)));
  chart?.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'line',
        smooth: true,
        data: values,
        areaStyle: {}
      }
    ]
  });
};

onMounted(() => {
  initRange();
  initChart();
  load();
  window.addEventListener('resize', () => chart?.resize());
});

watch(
  () => query.value.type,
  () => load()
);
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart {
  height: 360px;
}
</style>
