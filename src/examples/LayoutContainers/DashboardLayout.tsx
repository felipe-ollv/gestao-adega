import { ReactNode } from "react";

import Box from "@mui/material/Box";

import { useMaterialUIController } from "context";

function DashboardLayout({ children }: { children: ReactNode }) {
  const [{ miniSidenav }] = useMaterialUIController();

  return (
    <Box
      component="main"
      sx={{
        minHeight: "100vh",
        bgcolor: "#f5f7fb",
        ml: { xs: 0, lg: miniSidenav ? "88px" : "260px" },
        px: { xs: 2, md: 3 },
        pb: { xs: "calc(88px + env(safe-area-inset-bottom))", lg: 0 },
        transition: "margin-left 180ms ease",
      }}
    >
      {children}
    </Box>
  );
}

export default DashboardLayout;
