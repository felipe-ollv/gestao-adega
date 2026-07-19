import { NavLink } from "react-router-dom";

import Box from "@mui/material/Box";
import ButtonBase from "@mui/material/ButtonBase";
import Divider from "@mui/material/Divider";

import MDTypography from "components/MDTypography";
import { useUser } from "context/user.context";
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
  const { userData } = useUser();
  const displayBrandName = userData?.adegaNome || brandName || "Adega";
  const groups = Array.isArray(userData?.groups)
    ? userData.groups
    : typeof userData?.groups === "string"
      ? [userData.groups]
      : [];
  const userRole = userData?.perfil || userData?.ts;
  const navRoutes = routes.filter(
    (route) =>
      route.type === "collapse" &&
      route.route &&
      (!route.roles?.length ||
        route.roles.includes(userRole) ||
        route.roles.some((role) => groups.includes(role)))
  );

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
      <MDTypography variant="h5" color="grey.500" fontWeight="bold" px={1} py={1.5}>
        {displayBrandName}
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
