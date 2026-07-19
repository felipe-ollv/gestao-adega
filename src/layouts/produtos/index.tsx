import { FormEvent, useEffect, useState } from "react";

import Alert from "@mui/material/Alert";
import Card from "@mui/material/Card";
import Chip from "@mui/material/Chip";
import CircularProgress from "@mui/material/CircularProgress";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Grid from "@mui/material/Grid";
import Icon from "@mui/material/Icon";
import IconButton from "@mui/material/IconButton";
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
import { Produto, getApiErrorMessage, produtosApi } from "services/adega";

const currency = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

const emptyForm = {
  nome: "",
  quantidadeEstoqueUnidades: 0,
  alertaEstoqueUnidades: 12,
  unidadesPorCaixa: 1,
  valorUnidade: 0,
  valorCaixa: "",
};

function Produtos() {
  const { isGestor } = useUser();
  const [produtos, setProdutos] = useState<Produto[]>([]);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<Produto | null>(null);
  const [form, setForm] = useState<any>(emptyForm);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const [deletingUuid, setDeletingUuid] = useState<string | null>(null);

  const actionLoading = saving || Boolean(deletingUuid);

  const loadProdutos = async () => {
    setError("");
    try {
      setProdutos(await produtosApi.list());
    } catch (loadError) {
      setError(getApiErrorMessage(loadError));
    }
  };

  useEffect(() => {
    loadProdutos();
  }, []);

  const openCreate = () => {
    setEditing(null);
    setForm(emptyForm);
    setDialogOpen(true);
  };

  const openEdit = (produto: Produto) => {
    setEditing(produto);
    setForm({
      nome: produto.nome,
      quantidadeEstoqueUnidades: produto.quantidadeEstoqueUnidades,
      alertaEstoqueUnidades: produto.alertaEstoqueUnidades,
      unidadesPorCaixa: produto.unidadesPorCaixa,
      valorUnidade: produto.valorUnidade,
      valorCaixa: produto.valorCaixa ?? "",
    });
    setDialogOpen(true);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSaving(true);
    setError("");

    const payload = {
      nome: form.nome,
      quantidadeEstoqueUnidades: Number(form.quantidadeEstoqueUnidades),
      alertaEstoqueUnidades: Number(form.alertaEstoqueUnidades),
      unidadesPorCaixa: Number(form.unidadesPorCaixa),
      valorUnidade: Number(form.valorUnidade),
      valorCaixa: form.valorCaixa === "" ? null : Number(form.valorCaixa),
    };

    try {
      if (editing) {
        await produtosApi.update(editing.uuid, payload);
      } else {
        await produtosApi.create(payload);
      }
      setDialogOpen(false);
      await loadProdutos();
    } catch (submitError) {
      setError(getApiErrorMessage(submitError));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (produto: Produto) => {
    setError("");
    setDeletingUuid(produto.uuid);
    try {
      await produtosApi.delete(produto.uuid);
      await loadProdutos();
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
              Produtos
            </MDTypography>
            <MDTypography variant="button" color="text">
              Estoque controlado por unidades, com venda opcional por caixa.
            </MDTypography>
          </MDBox>
          {isGestor && (
            <MDButton variant="gradient" color="info" disabled={actionLoading} onClick={openCreate}>
              Novo produto
            </MDButton>
          )}
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
                  <TableCell>Produto</TableCell>
                  <TableCell>Estoque</TableCell>
                  <TableCell>Alerta estoque</TableCell>
                  <TableCell>Un./caixa</TableCell>
                  <TableCell>Valor unidade</TableCell>
                  <TableCell>Valor caixa</TableCell>
                  {isGestor && <TableCell align="right">Ações</TableCell>}
                </TableRow>
              </TableHead>
              <TableBody>
                {produtos.map((produto) => {
                  const isLowStock =
                    produto.quantidadeEstoqueUnidades <= produto.alertaEstoqueUnidades;

                  return (
                    <TableRow
                      key={produto.uuid}
                      sx={
                        isLowStock
                          ? {
                              bgcolor: "#fff7ed",
                              "&:hover": {
                                bgcolor: "#ffedd5",
                              },
                            }
                          : undefined
                      }
                    >
                      <TableCell>
                        <MDBox display="flex" alignItems="center" gap={1} flexWrap="wrap">
                          {produto.nome}
                          {isLowStock && (
                            <Chip
                              label="Estoque baixo"
                              size="small"
                              color="warning"
                              sx={{ fontWeight: 700 }}
                            />
                          )}
                        </MDBox>
                      </TableCell>
                      <TableCell>
                        <MDBox display="flex" alignItems="center" gap={1} flexWrap="wrap">
                          <MDTypography
                            variant="button"
                            fontWeight={isLowStock ? "bold" : "regular"}
                            color={isLowStock ? "warning" : "text"}
                          >
                            {produto.quantidadeEstoqueUnidades}
                          </MDTypography>
                          {isLowStock && (
                            <MDTypography variant="caption" color="warning" fontWeight="medium">
                              alerta {produto.alertaEstoqueUnidades}
                            </MDTypography>
                          )}
                        </MDBox>
                      </TableCell>
                      <TableCell>{produto.alertaEstoqueUnidades}</TableCell>
                      <TableCell>{produto.unidadesPorCaixa}</TableCell>
                      <TableCell>{currency.format(Number(produto.valorUnidade))}</TableCell>
                      <TableCell>
                        {produto.valorCaixa ? currency.format(Number(produto.valorCaixa)) : "-"}
                      </TableCell>
                      {isGestor && (
                        <TableCell align="right">
                          <Tooltip title="Editar">
                            <IconButton
                              color="info"
                              disabled={actionLoading}
                              onClick={() => openEdit(produto)}
                            >
                              <Icon>edit</Icon>
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Excluir">
                            <IconButton
                              color="error"
                              disabled={actionLoading}
                              onClick={() => handleDelete(produto)}
                            >
                              {deletingUuid === produto.uuid ? (
                                <CircularProgress color="inherit" size={20} />
                              ) : (
                                <Icon>delete</Icon>
                              )}
                            </IconButton>
                          </Tooltip>
                        </TableCell>
                      )}
                    </TableRow>
                  );
                })}
                {produtos.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={isGestor ? 7 : 6}>Nenhum produto cadastrado.</TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Card>
      </MDBox>

      <Dialog open={dialogOpen} onClose={() => !saving && setDialogOpen(false)} maxWidth="sm" fullWidth>
        <MDBox component="form" onSubmit={handleSubmit}>
          <DialogTitle>{editing ? "Editar produto" : "Novo produto"}</DialogTitle>
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
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Estoque em unidades"
                  type="number"
                  required
                  fullWidth
                  inputProps={{ min: 0 }}
                  value={form.quantidadeEstoqueUnidades}
                  onChange={(event) =>
                    setForm({ ...form, quantidadeEstoqueUnidades: event.target.value })
                  }
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Alerta estoque"
                  type="number"
                  required
                  fullWidth
                  helperText="Alerta quando o estoque for igual ou menor que este valor."
                  inputProps={{ min: 0 }}
                  value={form.alertaEstoqueUnidades}
                  onChange={(event) =>
                    setForm({ ...form, alertaEstoqueUnidades: event.target.value })
                  }
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Unidades por caixa"
                  type="number"
                  required
                  fullWidth
                  inputProps={{ min: 1 }}
                  value={form.unidadesPorCaixa}
                  onChange={(event) => setForm({ ...form, unidadesPorCaixa: event.target.value })}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Valor unidade"
                  type="number"
                  required
                  fullWidth
                  inputProps={{ min: 0.01, step: 0.01 }}
                  value={form.valorUnidade}
                  onChange={(event) => setForm({ ...form, valorUnidade: event.target.value })}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Valor caixa"
                  type="number"
                  fullWidth
                  inputProps={{ min: 0.01, step: 0.01 }}
                  value={form.valorCaixa}
                  onChange={(event) => setForm({ ...form, valorCaixa: event.target.value })}
                />
              </Grid>
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

export default Produtos;
