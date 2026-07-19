import { FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import Alert from "@mui/material/Alert";
import Card from "@mui/material/Card";
import Divider from "@mui/material/Divider";
import Grid from "@mui/material/Grid";
import Icon from "@mui/material/Icon";
import IconButton from "@mui/material/IconButton";
import InputAdornment from "@mui/material/InputAdornment";
import TextField from "@mui/material/TextField";
import ToggleButton from "@mui/material/ToggleButton";
import ToggleButtonGroup from "@mui/material/ToggleButtonGroup";

import MDBox from "components/MDBox";
import MDButton from "components/MDButton";
import MDTypography from "components/MDTypography";
import { paymentWhatsappUrl } from "config/payment";
import { setLayout, useMaterialUIController } from "context";
import { useUser } from "context/user.context";
import { authApi, getApiErrorMessage } from "services/adega";

const onlyDigits = (value: string) => value.replace(/\D/g, "").slice(0, 14);

const formatCpfCnpj = (value: string) => {
  const digits = onlyDigits(value);

  if (digits.length <= 11) {
    return digits
      .replace(/^(\d{3})(\d)/, "$1.$2")
      .replace(/^(\d{3})\.(\d{3})(\d)/, "$1.$2.$3")
      .replace(/\.(\d{3})(\d{1,2})$/, ".$1-$2");
  }

  return digits
    .replace(/^(\d{2})(\d)/, "$1.$2")
    .replace(/^(\d{2})\.(\d{3})(\d)/, "$1.$2.$3")
    .replace(/\.(\d{3})(\d)/, ".$1/$2")
    .replace(/(\d{4})(\d)/, "$1-$2");
};

function Login() {
  const [, dispatch] = useMaterialUIController();
  const navigate = useNavigate();
  const { saveTokenAndLogin } = useUser();
  const [mode, setMode] = useState<"login" | "register">("login");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [paymentPending, setPaymentPending] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [form, setForm] = useState({
    nomeAdega: "",
    cnpjCpf: "",
    nomeUsuario: "",
    email: "",
    senha: "",
  });

  useEffect(() => {
    setLayout(dispatch, "page");
    return () => setLayout(dispatch, "dashboard");
  }, [dispatch]);

  const updateField = (field: keyof typeof form, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const updateDocumentField = (value: string) => {
    updateField("cnpjCpf", formatCpfCnpj(value));
  };

  const handleModeChange = (_: unknown, value: "login" | "register" | null) => {
    if (!value) {
      return;
    }

    setMode(value);
    setError("");
    setPaymentPending(false);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setPaymentPending(false);
    setLoading(true);

    try {
      const response =
        mode === "login"
          ? await authApi.login(form.email, form.senha)
          : await authApi.register(form);

      if (response.statusPagamento === "PENDENTE" || !response.token) {
        setPaymentPending(true);
        return;
      }

      saveTokenAndLogin(response.token, response.statusPagamento);
      navigate("/dashboard", { replace: true });
    } catch (submitError) {
      setError(getApiErrorMessage(submitError));
    } finally {
      setLoading(false);
    }
  };

  return (
    <MDBox minHeight="100vh" sx={{ backgroundColor: "#111827" }}>
      <Grid container sx={{ minHeight: "100vh" }}>
        <Grid item xs={12} md={6}>
          <MDBox
            height="100%"
            display="flex"
            flexDirection="column"
            justifyContent="center"
            px={{ xs: 3, md: 8 }}
            py={{ xs: 6, md: 0 }}
          >
            <MDTypography variant="h1" color="grey.500" fontWeight="bold" mb={2}>
              Gestāo Comércio
            </MDTypography>
            <MDTypography variant="h5" color="grey.500" fontWeight="regular" sx={{ opacity: 0.82 }}>
              Gestão de produtos, estoque, comandas e permissões para
              gestores e atendentes.
            </MDTypography>
          </MDBox>
        </Grid>

        <Grid item xs={12} md={6}>
          <MDBox
            height="100%"
            display="flex"
            alignItems="center"
            justifyContent="center"
            px={{ xs: 3, md: 6 }}
            py={{ xs: 6, md: 0 }}
          >
            <Card sx={{ width: "100%", maxWidth: 460, p: { xs: 3, md: 4 } }}>
              <MDTypography variant="h4" fontWeight="medium" mb={1}>
                {mode === "login" ? "Entrar" : "Cadastrar Comércio"}
              </MDTypography>
              <MDTypography variant="button" color="text">
                Acesse o painel com um usuário gestor ou atendente.
              </MDTypography>

              <MDBox mt={3}>
                <ToggleButtonGroup
                  color="info"
                  exclusive
                  fullWidth
                  value={mode}
                  onChange={handleModeChange}
                >
                  <ToggleButton value="login">Login</ToggleButton>
                  <ToggleButton value="register">Novo Comércio</ToggleButton>
                </ToggleButtonGroup>
              </MDBox>

              <Divider sx={{ my: 3 }} />

              <MDBox component="form" onSubmit={handleSubmit}>
                {mode === "register" && (
                  <>
                    <TextField
                      label="Nome da adega"
                      fullWidth
                      required
                      margin="normal"
                      value={form.nomeAdega}
                      onChange={(event) => updateField("nomeAdega", event.target.value)}
                    />
                    <TextField
                      label="CPF ou CNPJ"
                      fullWidth
                      required
                      margin="normal"
                      inputProps={{ inputMode: "numeric", maxLength: 18 }}
                      value={form.cnpjCpf}
                      onChange={(event) => updateDocumentField(event.target.value)}
                    />
                    <TextField
                      label="Nome do gestor"
                      fullWidth
                      required
                      margin="normal"
                      value={form.nomeUsuario}
                      onChange={(event) => updateField("nomeUsuario", event.target.value)}
                    />
                  </>
                )}

                <TextField
                  label="E-mail"
                  type="email"
                  fullWidth
                  required
                  margin="normal"
                  value={form.email}
                  onChange={(event) => updateField("email", event.target.value)}
                />
                <TextField
                  label="Senha"
                  type={showPassword ? "text" : "password"}
                  fullWidth
                  required
                  margin="normal"
                  inputProps={{ minLength: mode === "register" ? 8 : undefined }}
                  InputProps={{
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          aria-label={showPassword ? "Ocultar senha" : "Mostrar senha"}
                          edge="end"
                          onClick={() => setShowPassword((current) => !current)}
                          onMouseDown={(event) => event.preventDefault()}
                        >
                          <Icon fontSize="small">{showPassword ? "visibility_off" : "visibility"}</Icon>
                        </IconButton>
                      </InputAdornment>
                    ),
                  }}
                  value={form.senha}
                  onChange={(event) => updateField("senha", event.target.value)}
                />

                {error && (
                  <Alert severity="error" sx={{ mt: 2 }}>
                    {error}
                  </Alert>
                )}

                {paymentPending && (
                  <Alert severity="warning" sx={{ mt: 2 }}>
                    Mensalidade pendente. Para liberar o acesso ao painel, clique para falar no
                    WhatsApp.
                    {paymentWhatsappUrl && (
                      <MDBox mt={1.5}>
                        <MDButton
                          component="a"
                          href={paymentWhatsappUrl}
                          target="_blank"
                          rel="noreferrer"
                          variant="contained"
                          color="success"
                          size="small"
                          fullWidth
                          sx={{
                            bgcolor: "#16a34a",
                            color: "#ffffff !important",
                            borderColor: "#16a34a",
                            boxShadow: "0 6px 14px rgba(22, 163, 74, 0.24)",
                            "&:hover": {
                              bgcolor: "#15803d",
                              borderColor: "#15803d",
                            },
                          }}
                        >
                          WhatsApp
                        </MDButton>
                      </MDBox>
                    )}
                  </Alert>
                )}

                <MDBox mt={3}>
                  <MDButton
                    type="submit"
                    variant="gradient"
                    color="info"
                    fullWidth
                    loading={loading}
                    loadingText="Processando..."
                  >
                    {mode === "login" ? "Entrar" : "Cadastrar comércio"}
                  </MDButton>
                </MDBox>
              </MDBox>
            </Card>
          </MDBox>
        </Grid>
      </Grid>
    </MDBox>
  );
}

export default Login;
