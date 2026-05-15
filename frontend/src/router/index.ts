import { isAuthenticated } from "@/api";
import {
	createRouter,
	createWebHistory,
	type RouteLocationNormalizedGeneric,
	type Router,
} from "vue-router";

const router: Router = createRouter({
	history: createWebHistory(import.meta.env.BASE_URL),
	routes: [
		{
			path: "/",
			name: "landing",
			component: () => import("@/views/LandingView.vue"),
			meta: {
				requiresAuth: true,
			},
		},
		{
			path: "/login",
			name: "login",
			component: () => import("@/views/LoginView.vue"),
		},
		{
			path: "/admin",
			name: "admin",
			component: () => import("@/views/AdminView.vue"),
			meta: {
				requiresAuth: true,
			},
		},
	],
});

router.beforeEach((to: RouteLocationNormalizedGeneric) => {
	if (to.meta.requiresAuth === true && isAuthenticated.value === false) {
		return { name: "login" };
	}
});

export default router;
