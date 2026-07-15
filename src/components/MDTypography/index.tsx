import { forwardRef } from "react";

import Typography, { TypographyProps } from "@mui/material/Typography";

type MDTypographyProps = TypographyProps & {
  fontWeight?: TypographyProps["fontWeight"];
  textTransform?: TypographyProps["textTransform"];
};

const MDTypography = forwardRef<HTMLElement, MDTypographyProps>(
  ({ color = "dark", fontWeight, textTransform, sx, ...rest }, ref) => (
    <Typography
      ref={ref}
      sx={{
        color: color === "text" ? "text.secondary" : color,
        fontWeight,
        textTransform,
        letterSpacing: 0,
        ...sx,
      }}
      {...rest}
    />
  )
);

MDTypography.displayName = "MDTypography";

export default MDTypography;
