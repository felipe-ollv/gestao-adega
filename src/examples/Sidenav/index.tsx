import { NavLink } from "react-router-dom";

import Box from "@mui/material/Box";
import ButtonBase from "@mui/material/ButtonBase";
import Divider from "@mui/material/Divider";

import MDTypography from "components/MDTypography";
import { AppRoute } from "routes";

type SidenavProps = {
  brandName: string;
  routes: AppRoute[];
  color?: string;
  brand?: string | null;
  onMouseEnter?: () => void;
  onMouseLeave?: () => void;
};

function Sidenav({ brandName, routes, onMouseEnter, onMouseLeave }: SidenavProps) {
  const navRoutes = routes.filter((route) => route.type === "collapse" && route.route);

  return (
    <Box
      component="aside"
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
      sx={{
        display: { xs: "none", lg: "block" },
        position: "fixed",
        inset: "16px auto 16px 16px",
        width: 228,
        borderRadius: 2,
        bgcolor: "#111827",
        color: "white",
        p: 2,
        zIndex: 1200,
      }}
    >
      <MDTypography variant="h5" color="white" fontWeight="bold" px={1} py={1.5}>
        {brandName}
      </MDTypography>
      <Divider sx={{ borderColor: "rgba(255,255,255,0.16)", my: 1 }} />
      <Box display="flex" flexDirection="column" gap={0.75} mt={2}>
        {navRoutes.map((route) => (
          <ButtonBase
            key={route.key}
            component={NavLink}
            to={route.route || "/"}
            sx={{
              justifyContent: "flex-start",
              gap: 1.25,
              px: 1.5,
              py: 1.1,
              borderRadius: 1,
              color: "rgba(255,255,255,0.78)",
              "&.active": {
                bgcolor: "info.main",
                color: "white",
              },
              "&:hover": {
                bgcolor: "rgba(255,255,255,0.12)",
              },
            }}
          >
            {route.icon}
            <MDTypography variant="button" color="inherit" fontWeight="medium">
              {route.name}
            </MDTypography>
          </ButtonBase>
        ))}
      </Box>
    </Box>
  );
}

export default Sidenav;
