import { useState, useEffect, useMemo, ReactNode } from "react";
import { Routes, Route, Navigate, useLocation } from "react-router-dom";

import { ThemeProvider } from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";

import Sidenav from "examples/Sidenav";
import MobileTabbar from "examples/MobileTabbar";
import MDSnackbar from "components/MDSnackbar";

import theme from "assets/theme";
import themeRTL from "assets/theme/theme-rtl";
import themeDark from "assets/theme-dark";
import themeDarkRTL from "assets/theme-dark/theme-rtl";

import { CacheProvider } from "@emotion/react";
import createCache, { EmotionCache } from "@emotion/cache";

import routes, { AppRoute } from "routes";
import { useMaterialUIController, setMiniSidenav } from "context";

import { useUser } from "context/user.context";
import { isJwtTokenValid } from "services/auth";

function ProtectedRoute({ children, roles }: { children: ReactNode; roles?: string[] }) {
  const { token, expireSession, userData } = useUser();
  const isAuthenticated = isJwtTokenValid(token);
  const groups = Array.isArray(userData?.groups)
    ? userData.groups
    : typeof userData?.groups === "string"
      ? [userData.groups]
      : [];
  const userRole = userData?.perfil || userData?.ts;
  const hasRole = !roles?.length || roles.includes(userRole) || roles.some((role) => groups.includes(role));

  useEffect(() => {
    if (token && !isAuthenticated) {
      expireSession();
    }
  }, [expireSession, isAuthenticated, token]);

  if (!isAuthenticated) {
    return <Navigate to="/entrar" replace />;
  }

  if (!hasRole) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
}

const getRoutes = (allRoutes: AppRoute[]): ReactNode =>
  allRoutes.flatMap((route) => {
    if (route.collapse) {
      return getRoutes(route.collapse);
    }

    if (route.route && route.component) {
      const element = route.public ? (
        route.component
      ) : (
        <ProtectedRoute roles={route.roles}>{route.component}</ProtectedRoute>
      );

      return <Route path={route.route} element={element} key={route.key} />;
    }

    return [];
  });

export default function App() {
  // @ts-ignore
  const [controller, dispatch] = useMaterialUIController();
  const { miniSidenav, direction, layout, sidenavColor, darkMode } = controller;
  const [onMouseEnter, setOnMouseEnter] = useState(false);
  const [showSessionNotice, setShowSessionNotice] = useState(false);
  const rtlCache = useMemo<EmotionCache>(() => createCache({ key: "rtl" }), []);
  const { pathname } = useLocation();

  const { token, shouldShowSessionNotice, consumeSessionNotice } = useUser();
  const hasValidSession = isJwtTokenValid(token);

  const handleOnMouseEnter = () => {
    if (miniSidenav && !onMouseEnter) {
      setMiniSidenav(dispatch, false);
      setOnMouseEnter(true);
    }
  };

  const handleOnMouseLeave = () => {
    if (onMouseEnter) {
      setMiniSidenav(dispatch, true);
      setOnMouseEnter(false);
    }
  };

  useEffect(() => {
    document.body.setAttribute("dir", direction);
  }, [direction]);

  useEffect(() => {
    document.documentElement.scrollTop = 0;
    document.scrollingElement.scrollTop = 0;
  }, [pathname]);

  useEffect(() => {
    if (token && shouldShowSessionNotice) {
      setShowSessionNotice(true);
      consumeSessionNotice();
    }
  }, [consumeSessionNotice, shouldShowSessionNotice, token]);

  const sidebar = layout === "dashboard" && (
    <Sidenav
      color={sidenavColor}
      brandName="Adega"
      brand={null}
      routes={routes}
      onMouseEnter={handleOnMouseEnter}
      onMouseLeave={handleOnMouseLeave}
    />
  );
  const mobileTabbar = layout === "dashboard" && hasValidSession && <MobileTabbar routes={routes} />;

  const appRoutes = (
    <Routes>
      {getRoutes(routes)}
      <Route
        path="/"
        element={
          hasValidSession ? <Navigate to="/dashboard" replace /> : <Navigate to="/entrar" replace />
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );

  const sessionNotice = (
    <MDSnackbar
      color="info"
      icon="schedule"
      title="Sessão iniciada"
      dateTime="Agora"
      content="Seu acesso será válido por 30 minutos. Após esse período, será necessário realizar um novo login."
      open={showSessionNotice}
      close={() => setShowSessionNotice(false)}
    />
  );

  if (direction === "rtl") {
    return (
      <CacheProvider value={rtlCache}>
        <ThemeProvider theme={darkMode ? themeDarkRTL : themeRTL}>
          <CssBaseline />
          {sidebar}
          {appRoutes}
          {mobileTabbar}
          {sessionNotice}
        </ThemeProvider>
      </CacheProvider>
    );
  }

  return (
    <ThemeProvider theme={darkMode ? themeDark : theme}>
      <CssBaseline />
      {sidebar}
      {appRoutes}
      {mobileTabbar}
      {sessionNotice}
    </ThemeProvider>
  );
}
