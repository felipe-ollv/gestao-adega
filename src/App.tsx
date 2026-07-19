import { useState, useEffect, ReactNode } from "react";
import { Routes, Route, Navigate, useLocation } from "react-router-dom";

import { ThemeProvider } from "@mui/material/styles";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import CssBaseline from "@mui/material/CssBaseline";
import Snackbar from "@mui/material/Snackbar";

import Sidenav from "examples/Sidenav";
import MobileTabbar from "examples/MobileTabbar";
import MDSnackbar from "components/MDSnackbar";

import theme from "assets/theme";

import routes, { AppRoute } from "routes";
import { useMaterialUIController, setMiniSidenav } from "context";

import { useUser } from "context/user.context";
import { isJwtTokenValid } from "services/auth";

const paymentWhatsappNumber = import.meta.env.VITE_PAYMENT_WHATSAPP_NUMBER || "";
const paymentWhatsappDigits = paymentWhatsappNumber.replace(/\D/g, "");
const paymentWhatsappMessage =
  import.meta.env.VITE_PAYMENT_WHATSAPP_MESSAGE ||
  "Olá, minha mensalidade está pendente na Gestão Comércio e gostaria de regularizar o acesso.";
const paymentWhatsappUrl = paymentWhatsappDigits
  ? `https://wa.me/${paymentWhatsappDigits}?text=${encodeURIComponent(paymentWhatsappMessage)}`
  : "";

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
  const { miniSidenav, layout, sidenavColor } = controller;
  const [onMouseEnter, setOnMouseEnter] = useState(false);
  const [showSessionNotice, setShowSessionNotice] = useState(false);
  const { pathname } = useLocation();

  const {
    token,
    shouldShowSessionNotice,
    shouldShowPaymentNotice,
    consumeSessionNotice,
    consumePaymentNotice,
  } = useUser();
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
      content="Seu acesso será válido por 4 horas. Após esse período, será necessário realizar um novo login."
      open={showSessionNotice}
      close={() => setShowSessionNotice(false)}
    />
  );

  const paymentNotice = (
    <Snackbar
      open={showSessionNotice ? false : shouldShowPaymentNotice}
      onClose={consumePaymentNotice}
      anchorOrigin={{ vertical: "top", horizontal: "right" }}
    >
      <Alert
        severity="warning"
        onClose={consumePaymentNotice}
        variant="filled"
        sx={{ width: "100%", maxWidth: 460, alignItems: "center" }}
        action={
          paymentWhatsappUrl ? (
            <Button color="inherit" size="small" href={paymentWhatsappUrl} target="_blank" rel="noreferrer">
              WhatsApp
            </Button>
          ) : undefined
        }
      >
        Mensalidade pendente. Para liberar o acesso ao painel, entre em contato pelo WhatsApp
        {paymentWhatsappNumber ? `: ${paymentWhatsappNumber}` : "."}
      </Alert>
    </Snackbar>
  );

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {sidebar}
      {appRoutes}
      {mobileTabbar}
      {sessionNotice}
      {paymentNotice}
    </ThemeProvider>
  );
}
