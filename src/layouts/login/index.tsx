import { FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import Alert from "@mui/material/Alert";
import Card from "@mui/material/Card";
import Divider from "@mui/material/Divider";
import Grid from "@mui/material/Grid";
import TextField from "@mui/material/TextField";
import ToggleButton from "@mui/material/ToggleButton";
import ToggleButtonGroup from "@mui/material/ToggleButtonGroup";

import MDBox from "components/MDBox";
import MDButton from "components/MDButton";
import MDTypography from "components/MDTypography";
import { setLayout, useMaterialUIController } from "context";
import { useUser } from "context/user.context";
import { authApi, getApiErrorMessage } from "services/adega";

function Login() {
  const [, dispatch] = useMaterialUIController();
  const navigate = useNavigate();
  const { saveTokenAndLogin } = useUser();
  const [mode, setMode] = useState<"login" | "register">("login");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
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

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response =
        mode === "login"
          ? await authApi.login(form.email, form.senha)
          : await authApi.register(form);

      saveTokenAndLogin(response.token);
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
            <MDTypography variant="h1" color="white" fontWeight="bold" mb={2}>
              Adega
            </MDTypography>
            <MDTypography variant="h5" color="white" fontWeight="regular" sx={{ opacity: 0.82 }}>
              Gestão de produtos, estoque unificado por unidade, comandas e permissões para
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
                {mode === "login" ? "Entrar" : "Cadastrar adega"}
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
                  onChange={(_, value) => value && setMode(value)}
                >
                  <ToggleButton value="login">Login</ToggleButton>
                  <ToggleButton value="register">Nova adega</ToggleButton>
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
                      label="CNPJ ou CPF"
                      fullWidth
                      required
                      margin="normal"
                      value={form.cnpjCpf}
                      onChange={(event) => updateField("cnpjCpf", event.target.value)}
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
                  type="password"
                  fullWidth
                  required
                  margin="normal"
                  inputProps={{ minLength: mode === "register" ? 8 : undefined }}
                  value={form.senha}
                  onChange={(event) => updateField("senha", event.target.value)}
                />

                {error && (
                  <Alert severity="error" sx={{ mt: 2 }}>
                    {error}
                  </Alert>
                )}

                <MDBox mt={3}>
                  <MDButton type="submit" variant="gradient" color="info" fullWidth disabled={loading}>
                    {loading ? "Processando..." : mode === "login" ? "Entrar" : "Cadastrar e entrar"}
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
