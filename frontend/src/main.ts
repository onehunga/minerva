import { createApp, type App as VueApp } from "vue";
import { createPinia, type Pinia } from "pinia";

import { initializeUserSession, UserRepository, UserRepositoryKey } from "@/feature/user";
import { ProjectRepository, ProjectRepositoryKey } from "@/feature/project";
import { TicketRepository, TicketRepositoryKey } from "@/feature/ticket";
import { ActivityRepository, ActivityRepositoryKey } from "@/feature/activity";
import { DashboardRepository, DashboardRepositoryKey } from "@/feature/dashboard";

import App from "./App.vue";
import router from "./router";
import "./style.css";

const app: VueApp<Element> = createApp(App);
const pinia: Pinia = createPinia();

app.use(pinia);

await initializeUserSession();

app.use(router);

app.provide(ProjectRepositoryKey, new ProjectRepository());
app.provide(TicketRepositoryKey, new TicketRepository());
app.provide(ActivityRepositoryKey, new ActivityRepository());
app.provide(UserRepositoryKey, new UserRepository());
app.provide(DashboardRepositoryKey, new DashboardRepository());

app.mount("#app");
