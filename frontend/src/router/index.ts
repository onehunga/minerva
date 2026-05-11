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
	],
});

router.beforeEach((to: RouteLocationNormalizedGeneric) => {
	if (to.meta.requiresAuth === true && isAuthenticated.value === false) {
		return { name: "login" };
	}
});

export default router;
