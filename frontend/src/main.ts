import { createApp, type App as VueApp } from "vue";
import { createPinia } from "pinia";

import App from "./App.vue";
import router from "./router";

const app: VueApp<Element> = createApp(App);

app.use(createPinia());
app.use(router);

app.mount("#app");
