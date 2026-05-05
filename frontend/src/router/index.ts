import { createRouter, createWebHistory, type Router } from "vue-router";

const router: Router = createRouter({
	history: createWebHistory(import.meta.env.BASE_URL),
	routes: [
		{
			path: "/",
			name: "landing",
			component: () => import("@/views/LandingView.vue"),
		},
		{
			path: "/login",
			name: "login",
			component: () => import("@/views/LoginView.vue"),
		},
	],
});

export default router;
