import Box from "@mui/material/Box";

import MDTypography from "components/MDTypography";

function Footer() {
  return (
    <Box component="footer" py={3}>
      <MDTypography variant="caption" color="text">
        Adega
      </MDTypography>
    </Box>
  );
}

export default Footer;
