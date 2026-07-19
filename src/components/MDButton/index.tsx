import { ReactNode, forwardRef } from "react";

import Button, { ButtonProps } from "@mui/material/Button";
import CircularProgress from "@mui/material/CircularProgress";

type MDButtonProps = ButtonProps & {
  variant?: ButtonProps["variant"] | "gradient";
  loading?: boolean;
  loadingText?: ReactNode;
};

const MDButton = forwardRef<HTMLButtonElement, MDButtonProps>(
  (
    {
      variant = "contained",
      color = "primary",
      sx,
      disabled,
      loading = false,
      loadingText,
      startIcon,
      children,
      ...rest
    },
    ref
  ) => {
    const muiVariant = variant === "gradient" ? "contained" : variant;

    return (
      <Button
        ref={ref}
        variant={muiVariant}
        color={color}
        disabled={disabled || loading}
        startIcon={loading ? <CircularProgress color="inherit" size={16} /> : startIcon}
        sx={{
          borderRadius: 1,
          textTransform: "none",
          fontWeight: 600,
          letterSpacing: 0,
          ...sx,
        }}
        {...rest}
      >
        {loading ? loadingText || children : children}
      </Button>
    );
  }
);

MDButton.displayName = "MDButton";

export default MDButton;
