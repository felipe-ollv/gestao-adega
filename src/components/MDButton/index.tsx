import { forwardRef } from "react";

import Button, { ButtonProps } from "@mui/material/Button";

type MDButtonProps = ButtonProps & {
  variant?: ButtonProps["variant"] | "gradient";
};

const MDButton = forwardRef<HTMLButtonElement, MDButtonProps>(
  ({ variant = "contained", color = "primary", sx, ...rest }, ref) => {
    const muiVariant = variant === "gradient" ? "contained" : variant;

    return (
      <Button
        ref={ref}
        variant={muiVariant}
        color={color}
        sx={{
          borderRadius: 1,
          textTransform: "none",
          fontWeight: 600,
          letterSpacing: 0,
          ...sx,
        }}
        {...rest}
      />
    );
  }
);

MDButton.displayName = "MDButton";

export default MDButton;
