import { createApp, type App as VueApp } from "vue";
import { createPinia } from "pinia";

import { initializeAuth } from "@/api";

import App from "./App.vue";
import router from "./router";

await initializeAuth();

const app: VueApp<Element> = createApp(App);

app.use(createPinia());
app.use(router);

app.mount("#app");
