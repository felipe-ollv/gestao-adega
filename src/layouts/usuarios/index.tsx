import { FormEvent, useEffect, useState } from "react";

import Alert from "@mui/material/Alert";
import Card from "@mui/material/Card";
import CircularProgress from "@mui/material/CircularProgress";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import FormControl from "@mui/material/FormControl";
import Grid from "@mui/material/Grid";
import Icon from "@mui/material/Icon";
import IconButton from "@mui/material/IconButton";
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
import Tooltip from "@mui/material/Tooltip";

import MDBox from "components/MDBox";
import MDButton from "components/MDButton";
import MDTypography from "components/MDTypography";
import { useUser } from "context/user.context";
import DashboardLayout from "examples/LayoutContainers/DashboardLayout";
import DashboardNavbar from "examples/Navbars/DashboardNavbar";
import Footer from "examples/Footer";
import { PerfilUsuario, Usuario, getApiErrorMessage, usuariosApi } from "services/adega";

const emptyForm = {
  nome: "",
  email: "",
  senha: "",
  perfil: "ATENDENTE" as PerfilUsuario,
  ativo: true,
};

function Usuarios() {
  const { userData } = useUser();
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [form, setForm] = useState(emptyForm);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<Usuario | null>(null);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const [deletingUuid, setDeletingUuid] = useState<string | null>(null);

  const actionLoading = saving || Boolean(deletingUuid);

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

  const openCreate = () => {
    setEditing(null);
    setForm(emptyForm);
    setDialogOpen(true);
  };

  const openEdit = (usuario: Usuario) => {
    setEditing(usuario);
    setForm({
      nome: usuario.nome,
      email: usuario.email,
      senha: "",
      perfil: usuario.perfil,
      ativo: usuario.ativo,
    });
    setDialogOpen(true);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSaving(true);
    setError("");

    try {
      if (editing) {
        await usuariosApi.update(editing.uuid, {
          nome: form.nome,
          email: form.email,
          perfil: form.perfil,
          ativo: form.ativo,
          ...(form.senha ? { senha: form.senha } : {}),
        });
      } else {
        await usuariosApi.create({
          nome: form.nome,
          email: form.email,
          senha: form.senha,
          perfil: form.perfil,
        });
      }
      setDialogOpen(false);
      setForm(emptyForm);
      setEditing(null);
      await loadUsuarios();
    } catch (submitError) {
      setError(getApiErrorMessage(submitError));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (usuario: Usuario) => {
    setError("");
    setDeletingUuid(usuario.uuid);
    try {
      await usuariosApi.delete(usuario.uuid);
      await loadUsuarios();
    } catch (deleteError) {
      setError(getApiErrorMessage(deleteError));
    } finally {
      setDeletingUuid(null);
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
          <MDButton variant="gradient" color="info" disabled={actionLoading} onClick={openCreate}>
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
                  <TableCell align="right">Ações</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {usuarios.map((usuario) => {
                  const isCurrentUser = usuario.uuid === userData?.sub;

                  return (
                    <TableRow key={usuario.uuid}>
                      <TableCell>{usuario.nome}</TableCell>
                      <TableCell>{usuario.email}</TableCell>
                      <TableCell>{usuario.perfil}</TableCell>
                      <TableCell>{usuario.ativo ? "Ativo" : "Inativo"}</TableCell>
                      <TableCell align="right">
                        <Tooltip title="Editar">
                          <IconButton
                            color="info"
                            disabled={actionLoading}
                            onClick={() => openEdit(usuario)}
                          >
                            <Icon>edit</Icon>
                          </IconButton>
                        </Tooltip>
                        <Tooltip title={isCurrentUser ? "Não é possível excluir seu próprio usuário" : "Excluir"}>
                          <span>
                            <IconButton
                              color="error"
                              disabled={isCurrentUser || actionLoading}
                              onClick={() => handleDelete(usuario)}
                            >
                              {deletingUuid === usuario.uuid ? (
                                <CircularProgress color="inherit" size={20} />
                              ) : (
                                <Icon>delete</Icon>
                              )}
                            </IconButton>
                          </span>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  );
                })}
                {usuarios.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5}>Nenhum usuário cadastrado.</TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Card>
      </MDBox>

      <Dialog open={dialogOpen} onClose={() => !saving && setDialogOpen(false)} maxWidth="sm" fullWidth>
        <MDBox component="form" onSubmit={handleSubmit}>
          <DialogTitle>{editing ? "Editar usuário" : "Novo usuário"}</DialogTitle>
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
                  required={!editing}
                  fullWidth
                  inputProps={{ minLength: 8 }}
                  value={form.senha}
                  onChange={(event) => setForm({ ...form, senha: event.target.value })}
                  helperText={editing ? "Preencha apenas se quiser alterar a senha." : ""}
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
              {editing && (
                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth>
                    <InputLabel>Status</InputLabel>
                    <Select
                      label="Status"
                      value={form.ativo ? "ativo" : "inativo"}
                      onChange={(event) =>
                        setForm({ ...form, ativo: event.target.value === "ativo" })
                      }
                    >
                      <MenuItem value="ativo">Ativo</MenuItem>
                      <MenuItem value="inativo">Inativo</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
              )}
            </Grid>
          </DialogContent>
          <DialogActions>
            <MDButton variant="text" color="secondary" disabled={saving} onClick={() => setDialogOpen(false)}>
              Cancelar
            </MDButton>
            <MDButton type="submit" variant="gradient" color="info" loading={saving} loadingText="Salvando...">
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
