import { ref } from "vue";
import { defineStore } from "pinia";
import deepmerge from "deepmerge";
import objectPath from "object-path";
import DefaultLayoutConfig from "@/app/layouts/default/config/DefaultLayoutConfig";
import type LayoutConfigTypes from "@/app/layouts/default/config/types";

export const LS_CONFIG_NAME_KEY = "config_" + import.meta.env.VITE_APP_DEMO;

const cloneConfig = (source: LayoutConfigTypes): LayoutConfigTypes =>
  deepmerge({} as LayoutConfigTypes, source);

const readStoredConfig = (): Partial<LayoutConfigTypes> => {
  try {
    return JSON.parse(window.localStorage.getItem(LS_CONFIG_NAME_KEY) || "{}");
  } catch {
    return {};
  }
};

export const useConfigStore = defineStore("config", () => {
  const config = ref<LayoutConfigTypes>(cloneConfig(DefaultLayoutConfig));
  const initial = ref<LayoutConfigTypes>(cloneConfig(DefaultLayoutConfig));

  function getLayoutConfig(path: string, defaultValue?: string) {
    return objectPath.get(config.value, path, defaultValue);
  }

  function setLayoutConfigProperty(property: string, value: any) {
    objectPath.set(config.value, property, value);
    localStorage.setItem(LS_CONFIG_NAME_KEY, JSON.stringify(config.value));
  }

  function resetLayoutConfig() {
    config.value = cloneConfig(initial.value);
  }

  function overrideLayoutConfig() {
    initial.value = deepmerge(cloneConfig(DefaultLayoutConfig), readStoredConfig());
    config.value = cloneConfig(initial.value);
  }

  return {
    config,
    getLayoutConfig,
    setLayoutConfigProperty,
    resetLayoutConfig,
    overrideLayoutConfig,
  };
});
