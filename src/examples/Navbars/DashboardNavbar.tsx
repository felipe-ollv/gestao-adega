import { useLocation } from "react-router-dom";

import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";

import MDTypography from "components/MDTypography";
import { useUser } from "context/user.context";

const titles: Record<string, string> = {
  "/dashboard": "Dashboard",
  "/comandas": "Comandas",
  "/produtos": "Produtos",
  "/usuarios": "Usuarios",
};

function DashboardNavbar() {
  const { pathname } = useLocation();
  const { userData } = useUser();

  return (
    <AppBar
      position="sticky"
      color="transparent"
      elevation={0}
      sx={{ backdropFilter: "blur(10px)", py: 1 }}
    >
      <Toolbar disableGutters sx={{ justifyContent: "space-between" }}>
        <MDTypography variant="h6" fontWeight="medium">
          {titles[pathname] || "Adega"}
        </MDTypography>
        <MDTypography variant="button" color="text">
          {userData?.nome || userData?.upn || userData?.sub || ""}
        </MDTypography>
      </Toolbar>
    </AppBar>
  );
}

export default DashboardNavbar;
