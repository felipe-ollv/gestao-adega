import Alert from "@mui/material/Alert";
import Snackbar from "@mui/material/Snackbar";

type MDSnackbarProps = {
  color?: "success" | "info" | "warning" | "error";
  title?: string;
  content?: string;
  open: boolean;
  close: () => void;
  icon?: string;
  dateTime?: string;
};

function MDSnackbar({ color = "info", title, content, open, close }: MDSnackbarProps) {
  return (
    <Snackbar open={open} autoHideDuration={5000} onClose={close} anchorOrigin={{ vertical: "top", horizontal: "right" }}>
      <Alert severity={color} onClose={close} variant="filled" sx={{ width: "100%" }}>
        {title ? `${title}. ` : ""}
        {content}
      </Alert>
    </Snackbar>
  );
}

export default MDSnackbar;
