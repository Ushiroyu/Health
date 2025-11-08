<template>
  <div class="layout">
    <a class="skip-link" href="#main-content">跳到主要内容</a>
    <main-sidebar
      :collapsed="collapsed"
      :is-mobile="isMobile"
      @toggle="handleToggleSidebar"
    />
    <div class="layout__main">
      <top-navbar @toggle-sidebar="handleToggleSidebar" />
      <el-scrollbar class="layout__content">
        <div id="main-content" class="layout__container" tabindex="-1">
          <router-view />
        </div>
      </el-scrollbar>
    </div>
    <transition name="fade">
      <button
        v-if="isMobile && !collapsed"
        class="layout__overlay"
        type="button"
        aria-label="关闭侧边导航"
        @click="handleToggleSidebar"
      ></button>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { useBreakpoints } from '@vueuse/core';
import MainSidebar from '@/components/MainSidebar.vue';
import TopNavbar from '@/components/TopNavbar.vue';

const breakpoints = useBreakpoints({
  sm: 640,
  md: 768,
  lg: 1024,
  xl: 1280
});

const isMobile = computed(() => breakpoints.smaller('lg').value);
const collapsed = ref(true);

const ensureState = () => {
  collapsed.value = isMobile.value;
};

watch(isMobile, ensureState, { immediate: true });

const handleToggleSidebar = () => {
  collapsed.value = !collapsed.value;
};

if (typeof window !== 'undefined') {
  const onResize = () => ensureState();
  window.addEventListener('orientationchange', onResize);
  onBeforeUnmount(() => {
    window.removeEventListener('orientationchange', onResize);
  });
}
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  position: relative;
}

.skip-link {
  position: absolute;
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
  padding: 10px 16px;
  background: var(--app-primary);
  color: #fff;
  border-radius: 999px;
  font-size: 13px;
  opacity: 0;
  pointer-events: none;
  transition: opacity var(--transition-base), transform var(--transition-base);
  z-index: 20;
}

.skip-link:focus {
  opacity: 1;
  pointer-events: auto;
  transform: translate(-50%, 0);
  box-shadow: var(--focus-ring);
}

.layout__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  position: relative;
  backdrop-filter: blur(18px);
  transition: margin-left var(--transition-base);
}

.layout__content {
  flex: 1;
  padding: 32px 36px 40px;
}

.layout__container {
  min-height: calc(100vh - 120px);
  background: var(--app-surface);
  border-radius: calc(var(--card-radius) + 6px);
  box-shadow: var(--card-shadow);
  padding: 28px 32px 40px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(14px);
  transition: background var(--transition-base), border-color var(--transition-base),
    box-shadow var(--transition-base);
}

.layout__overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  z-index: 12;
  border: none;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 1280px) {
  .layout__content {
    padding: 28px;
  }

  .layout__container {
    padding: 24px;
  }
}

@media (max-width: 1024px) {
  .layout {
    flex-direction: column;
  }

  .layout__content {
    padding: 24px 20px 28px;
  }

  .layout__container {
    padding: 20px;
  }
}

@media (max-width: 640px) {
  .layout__content {
    padding: 18px 16px 24px;
  }

  .layout__container {
    padding: 18px;
  }
}
</style>
