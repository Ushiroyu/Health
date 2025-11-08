<template>
  <div class="stats">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>健康记录统计</span>
          <el-space>
            <el-select v-model="healthParams.type" placeholder="类型" style="width: 140px">
              <el-option label="血压" value="BP" />
              <el-option label="血糖" value="BG" />
              <el-option label="体重" value="WEIGHT" />
            </el-select>
            <el-select v-model="healthParams.bucket" style="width: 140px">
              <el-option label="按日" value="day" />
              <el-option label="按周" value="week" />
              <el-option label="按月" value="month" />
            </el-select>
            <el-button type="primary" @click="loadHealth">刷新</el-button>
          </el-space>
        </div>
      </template>
      <div ref="healthChartRef" class="chart" aria-label="健康记录统计图表"></div>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>异常率统计</span>
          <el-space>
            <el-select v-model="healthParams.type" placeholder="类型" style="width: 140px">
              <el-option label="血压" value="BP" />
              <el-option label="血糖" value="BG" />
              <el-option label="体重" value="WEIGHT" />
            </el-select>
            <el-button type="primary" @click="loadAbnormal">刷新</el-button>
          </el-space>
        </div>
      </template>
      <div ref="abnormalChartRef" class="chart" aria-label="异常率趋势图"></div>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>预约统计</span>
          <el-space>
            <el-date-picker
              v-model="appointmentRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            />
            <el-select v-model="appointmentBucket" style="width: 140px">
              <el-option label="按日" value="day" />
              <el-option label="按周" value="week" />
              <el-option label="按月" value="month" />
            </el-select>
            <el-button type="primary" @click="loadAppointment">刷新</el-button>
          </el-space>
        </div>
      </template>
      <div ref="appointmentChartRef" class="chart" aria-label="预约统计图表"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { usePreferredReducedMotion } from '@vueuse/core';
import { fetchHealthRecordStats, fetchAbnormalRateStats } from '@/services/admin';
import { fetchAppointmentStats } from '@/services/appointment';

import type { ECharts } from 'echarts';

const healthChartRef = ref<HTMLDivElement>();
const abnormalChartRef = ref<HTMLDivElement>();
const appointmentChartRef = ref<HTMLDivElement>();

let echartsModule: typeof import('echarts') | null = null;
let healthChart: ECharts | null = null;
let abnormalChart: ECharts | null = null;
let appointmentChart: ECharts | null = null;
let resizeHandler: (() => void) | null = null;

const prefersReducedMotion = usePreferredReducedMotion();

const healthParams = ref({
  type: 'BP',
  bucket: 'day'
});

const appointmentRange = ref<[string, string] | null>(null);
const appointmentBucket = ref<'day' | 'week' | 'month'>('day');

const ensureEcharts = async () => {
  if (!echartsModule) {
    echartsModule = await import('echarts');
  }
  return echartsModule;
};

const getThemeColor = (variable: string, fallback: string) => {
  if (typeof window === 'undefined') return fallback;
  const value = getComputedStyle(document.documentElement).getPropertyValue(variable);
  return value ? value.trim() : fallback;
};

const ensureCharts = async () => {
  const echarts = await ensureEcharts();

  if (!healthChart && healthChartRef.value) {
    healthChart = echarts.init(healthChartRef.value);
  }
  if (!abnormalChart && abnormalChartRef.value) {
    abnormalChart = echarts.init(abnormalChartRef.value);
  }
  if (!appointmentChart && appointmentChartRef.value) {
    appointmentChart = echarts.init(appointmentChartRef.value);
  }

  if (!resizeHandler && typeof window !== 'undefined') {
    resizeHandler = () => {
      healthChart?.resize();
      abnormalChart?.resize();
      appointmentChart?.resize();
    };
    window.addEventListener('resize', resizeHandler);
  }
};

const initRange = () => {
  const end = new Date();
  const start = new Date(end);
  start.setDate(end.getDate() - 30);
  appointmentRange.value = [start.toISOString().slice(0, 10), end.toISOString().slice(0, 10)];
};

const animationConfig = () => {
  const reduce = prefersReducedMotion.value === 'reduce';
  const easing = reduce ? 'linear' : 'cubicOut';
  return {
    animationDuration: reduce ? 0 : 600,
    animationEasing: easing as 'linear' | 'cubicOut'
  };
};

const loadHealth = async () => {
  if (!appointmentRange.value) return;
  await ensureCharts();
  const data = await fetchHealthRecordStats({
    type: healthParams.value.type,
    from: appointmentRange.value[0],
    to: appointmentRange.value[1],
    bucket: healthParams.value.bucket as 'day' | 'week' | 'month'
  });

  const primary = getThemeColor('--app-primary', '#4f46e5');
  const accent = getThemeColor('--app-accent', '#14b8a6');

  healthChart?.setOption({
    ...animationConfig(),
    tooltip: { trigger: 'axis' },
    grid: { left: 32, right: 24, top: 36, bottom: 32 },
    xAxis: {
      type: 'category',
      data: data.map((item) => item.bucket),
      boundaryGap: true,
      axisLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.35)' } },
      axisLabel: { color: 'var(--app-muted)' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.15)' } },
      axisLabel: { color: 'var(--app-muted)' }
    },
    series: [
      {
        type: 'bar',
        data: data.map((item) => item.count),
        itemStyle: {
          borderRadius: [12, 12, 4, 4],
          color: primary
        },
        emphasis: {
          itemStyle: {
            color: accent
          }
        }
      }
    ]
  });
};

const loadAbnormal = async () => {
  if (!appointmentRange.value) return;
  await ensureCharts();
  const data = await fetchAbnormalRateStats({
    type: healthParams.value.type,
    from: appointmentRange.value[0],
    to: appointmentRange.value[1],
    bucket: healthParams.value.bucket as 'day' | 'week' | 'month'
  });

  const echarts = await ensureEcharts();
  const primary = getThemeColor('--app-primary', '#4f46e5');

  abnormalChart?.setOption({
    ...animationConfig(),
    tooltip: { trigger: 'axis', valueFormatter: (val: number) => `${(val * 100).toFixed(1)}%` },
    grid: { left: 32, right: 32, top: 36, bottom: 32 },
    xAxis: {
      type: 'category',
      data: data.map((item) => item.bucket),
      axisLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.35)' } },
      axisLabel: { color: 'var(--app-muted)' }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 1,
      axisLabel: {
        formatter: (value: number) => `${(value * 100).toFixed(0)}%`,
        color: 'var(--app-muted)'
      },
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.15)' } }
    },
    series: [
      {
        name: '异常率',
        type: 'line',
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 3, color: primary },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: `${primary}aa` },
            { offset: 1, color: 'transparent' }
          ])
        },
        data: data.map((item) => Number(item.rate.toFixed(3)))
      }
    ]
  });
};

const loadAppointment = async () => {
  if (!appointmentRange.value) return;
  await ensureCharts();
  const data = await fetchAppointmentStats({
    from: appointmentRange.value[0],
    to: appointmentRange.value[1],
    bucket: appointmentBucket.value
  });

  const primary = getThemeColor('--app-primary', '#4f46e5');
  const accent = getThemeColor('--app-accent', '#14b8a6');

  appointmentChart?.setOption({
    ...animationConfig(),
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['总预约', '取消'],
      textStyle: { color: 'var(--app-muted)' }
    },
    grid: { left: 32, right: 32, top: 40, bottom: 36 },
    xAxis: {
      type: 'category',
      data: data.map((item) => item.bucket),
      axisLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.35)' } },
      axisLabel: { color: 'var(--app-muted)' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.15)' } },
      axisLabel: { color: 'var(--app-muted)' }
    },
    series: [
      {
        name: '总预约',
        type: 'line',
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 3, color: primary },
        data: data.map((item) => item.total)
      },
      {
        name: '取消',
        type: 'line',
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 3, color: accent },
        areaStyle: {
          opacity: 0.12,
          color: accent
        },
        data: data.map((item) => item.canceled)
      }
    ]
  });
};

onMounted(async () => {
  initRange();
  await ensureCharts();
  await Promise.all([loadHealth(), loadAbnormal(), loadAppointment()]);
});

onBeforeUnmount(() => {
  healthChart?.dispose();
  abnormalChart?.dispose();
  appointmentChart?.dispose();
  healthChart = null;
  abnormalChart = null;
  appointmentChart = null;
  if (resizeHandler && typeof window !== 'undefined') {
    window.removeEventListener('resize', resizeHandler);
    resizeHandler = null;
  }
});
</script>

<style scoped>
.stats {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.chart {
  height: 320px;
}

@media (max-width: 768px) {
  .chart {
    height: 280px;
  }
}
</style>
