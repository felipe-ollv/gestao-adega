import Box from "@mui/material/Box";

import MDTypography from "components/MDTypography";
import { useUser } from "context/user.context";

function Footer() {
  const { userData } = useUser();
  const displayBrandName = userData?.adegaNome || "Comércio";

  return (
    <Box component="footer" py={3}>
      <MDTypography variant="caption" color="grey.500">
        {displayBrandName}
      </MDTypography>
    </Box>
  );
}

export default Footer;
