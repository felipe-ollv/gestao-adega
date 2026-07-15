import { FormEvent, useEffect, useState } from "react";

import Alert from "@mui/material/Alert";
import Card from "@mui/material/Card";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import FormControl from "@mui/material/FormControl";
import Grid from "@mui/material/Grid";
import InputLabel from "@mui/material/InputLabel";
import MenuItem from "@mui/material/MenuItem";
import Select from "@mui/material/Select";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TextField from "@mui/material/TextField";

import MDBox from "components/MDBox";
import MDButton from "components/MDButton";
import MDTypography from "components/MDTypography";
import DashboardLayout from "examples/LayoutContainers/DashboardLayout";
import DashboardNavbar from "examples/Navbars/DashboardNavbar";
import Footer from "examples/Footer";
import { PerfilUsuario, Usuario, getApiErrorMessage, usuariosApi } from "services/adega";

const emptyForm = {
  nome: "",
  email: "",
  senha: "",
  perfil: "ATENDENTE" as PerfilUsuario,
};

function Usuarios() {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [form, setForm] = useState(emptyForm);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const loadUsuarios = async () => {
    setError("");
    try {
      setUsuarios(await usuariosApi.list());
    } catch (loadError) {
      setError(getApiErrorMessage(loadError));
    }
  };

  useEffect(() => {
    loadUsuarios();
  }, []);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      await usuariosApi.create(form);
      setDialogOpen(false);
      setForm(emptyForm);
      await loadUsuarios();
    } catch (submitError) {
      setError(getApiErrorMessage(submitError));
    } finally {
      setLoading(false);
    }
  };

  return (
    <DashboardLayout>
      <DashboardNavbar />
      <MDBox py={3}>
        <MDBox mb={3} display="flex" justifyContent="space-between" alignItems="center" gap={2}>
          <MDBox>
            <MDTypography variant="h4" fontWeight="medium">
              Usuários
            </MDTypography>
            <MDTypography variant="button" color="text">
              Controle de acesso por perfil.
            </MDTypography>
          </MDBox>
          <MDButton variant="gradient" color="info" onClick={() => setDialogOpen(true)}>
            Novo usuário
          </MDButton>
        </MDBox>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Card>
          <TableContainer>
            <Table>
              <TableHead sx={{ display: "table-header-group" }}>
                <TableRow>
                  <TableCell>Nome</TableCell>
                  <TableCell>E-mail</TableCell>
                  <TableCell>Perfil</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {usuarios.map((usuario) => (
                  <TableRow key={usuario.uuid}>
                    <TableCell>{usuario.nome}</TableCell>
                    <TableCell>{usuario.email}</TableCell>
                    <TableCell>{usuario.perfil}</TableCell>
                    <TableCell>{usuario.ativo ? "Ativo" : "Inativo"}</TableCell>
                  </TableRow>
                ))}
                {usuarios.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={4}>Nenhum usuário cadastrado.</TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Card>
      </MDBox>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <MDBox component="form" onSubmit={handleSubmit}>
          <DialogTitle>Novo usuário</DialogTitle>
          <DialogContent>
            <Grid container spacing={2} mt={0.5}>
              <Grid item xs={12}>
                <TextField
                  label="Nome"
                  required
                  fullWidth
                  value={form.nome}
                  onChange={(event) => setForm({ ...form, nome: event.target.value })}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="E-mail"
                  type="email"
                  required
                  fullWidth
                  value={form.email}
                  onChange={(event) => setForm({ ...form, email: event.target.value })}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Senha"
                  type="password"
                  required
                  fullWidth
                  inputProps={{ minLength: 8 }}
                  value={form.senha}
                  onChange={(event) => setForm({ ...form, senha: event.target.value })}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth>
                  <InputLabel>Perfil</InputLabel>
                  <Select
                    label="Perfil"
                    value={form.perfil}
                    onChange={(event) =>
                      setForm({ ...form, perfil: event.target.value as PerfilUsuario })
                    }
                  >
                    <MenuItem value="ATENDENTE">Atendente</MenuItem>
                    <MenuItem value="GESTOR">Gestor</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <MDButton variant="text" color="secondary" onClick={() => setDialogOpen(false)}>
              Cancelar
            </MDButton>
            <MDButton type="submit" variant="gradient" color="info" disabled={loading}>
              Salvar
            </MDButton>
          </DialogActions>
        </MDBox>
      </Dialog>
      <Footer />
    </DashboardLayout>
  );
}

export default Usuarios;
