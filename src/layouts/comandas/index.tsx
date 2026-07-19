import { FormEvent, useEffect, useMemo, useState } from "react";

import Alert from "@mui/material/Alert";
import Autocomplete from "@mui/material/Autocomplete";
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
import TextField from "@mui/material/TextField";
import Tooltip from "@mui/material/Tooltip";
import ToggleButton from "@mui/material/ToggleButton";
import ToggleButtonGroup from "@mui/material/ToggleButtonGroup";

import MDBox from "components/MDBox";
import MDButton from "components/MDButton";
import MDTypography from "components/MDTypography";
import { useUser } from "context/user.context";
import DashboardLayout from "examples/LayoutContainers/DashboardLayout";
import DashboardNavbar from "examples/Navbars/DashboardNavbar";
import Footer from "examples/Footer";
import {
  Comanda,
  ComandaItem,
  Produto,
  TipoMedidaVenda,
  comandasApi,
  getApiErrorMessage,
  produtosApi,
} from "services/adega";

const currency = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
type LoadingAction = "open" | "add" | "update" | "close-paga" | "close-fiado" | `delete-${string}`;

function Comandas() {
  const { isGestor } = useUser();
  const [comandasAbertas, setComandasAbertas] = useState<Comanda[]>([]);
  const [comandasFiado, setComandasFiado] = useState<Comanda[]>([]);
  const [produtos, setProdutos] = useState<Produto[]>([]);
  const [selectedUuid, setSelectedUuid] = useState("");
  const [novoResponsavel, setNovoResponsavel] = useState("");
  const [produtoUuid, setProdutoUuid] = useState("");
  const [quantidade, setQuantidade] = useState(1);
  const [tipoMedida, setTipoMedida] = useState<TipoMedidaVenda>("UNIDADE");
  const [editingItem, setEditingItem] = useState<ComandaItem | null>(null);
  const [editProdutoUuid, setEditProdutoUuid] = useState("");
  const [editQuantidade, setEditQuantidade] = useState(1);
  const [editTipoMedida, setEditTipoMedida] = useState<TipoMedidaVenda>("UNIDADE");
  const [closingDialog, setClosingDialog] = useState(false);
  const [error, setError] = useState("");
  const [loadingAction, setLoadingAction] = useState<LoadingAction | null>(null);

  const allComandas = useMemo(
    () => [...comandasAbertas, ...comandasFiado],
    [comandasAbertas, comandasFiado]
  );

  const selectedComanda = useMemo(
    () => allComandas.find((comanda) => comanda.uuid === selectedUuid) || allComandas[0],
    [allComandas, selectedUuid]
  );

  const selectedProduto = produtos.find((produto) => produto.uuid === produtoUuid);
  const selectedEditProduto = produtos.find((produto) => produto.uuid === editProdutoUuid);
  const actionLoading = Boolean(loadingAction);

  const updateComandaState = (updated: Comanda) => {
    setComandasAbertas((current) =>
      updated.status === "ABERTA"
        ? current.map((comanda) => (comanda.uuid === updated.uuid ? updated : comanda))
        : current.filter((comanda) => comanda.uuid !== updated.uuid)
    );
    setComandasFiado((current) =>
      updated.status === "FIADO"
        ? current.map((comanda) => (comanda.uuid === updated.uuid ? updated : comanda))
        : current.filter((comanda) => comanda.uuid !== updated.uuid)
    );
  };

  const loadData = async () => {
    setError("");
    try {
      const [abertasData, fiadoData, produtosData] = await Promise.all([
        comandasApi.list("ABERTA"),
        comandasApi.list("FIADO"),
        produtosApi.list(),
      ]);
      setComandasAbertas(abertasData);
      setComandasFiado(fiadoData);
      setProdutos(produtosData);
      if (!selectedUuid && (abertasData[0] || fiadoData[0])) {
        setSelectedUuid((abertasData[0] || fiadoData[0]).uuid);
      }
    } catch (loadError) {
      setError(getApiErrorMessage(loadError));
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleOpenComanda = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!novoResponsavel.trim()) return;

    setLoadingAction("open");
    setError("");
    try {
      const comanda = await comandasApi.open(novoResponsavel);
      setComandasAbertas((current) => [comanda, ...current]);
      setSelectedUuid(comanda.uuid);
      setNovoResponsavel("");
    } catch (openError) {
      setError(getApiErrorMessage(openError));
    } finally {
      setLoadingAction(null);
    }
  };

  const handleAddItem = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!selectedComanda || !produtoUuid) return;

    setLoadingAction("add");
    setError("");
    try {
      const updated = await comandasApi.addItem(selectedComanda.uuid, {
        produtoUuid,
        quantidade: Number(quantidade),
        tipoMedida,
      });
      updateComandaState(updated);
      setProdutoUuid("");
      setQuantidade(1);
      setTipoMedida("UNIDADE");
      setProdutos(await produtosApi.list());
    } catch (addError) {
      setError(getApiErrorMessage(addError));
    } finally {
      setLoadingAction(null);
    }
  };

  const openEditItem = (item: ComandaItem) => {
    setEditingItem(item);
    setEditProdutoUuid(item.produtoUuid);
    setEditQuantidade(item.quantidadePedida);
    setEditTipoMedida(item.tipoMedida);
  };

  const closeEditItem = () => {
    setEditingItem(null);
    setEditProdutoUuid("");
    setEditQuantidade(1);
    setEditTipoMedida("UNIDADE");
  };

  const handleUpdateItem = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!selectedComanda || !editingItem || !editProdutoUuid) return;

    setLoadingAction("update");
    setError("");
    try {
      const updated = await comandasApi.updateItem(selectedComanda.uuid, editingItem.uuid, {
        produtoUuid: editProdutoUuid,
        quantidade: Number(editQuantidade),
        tipoMedida: editTipoMedida,
      });
      updateComandaState(updated);
      closeEditItem();
      setProdutos(await produtosApi.list());
    } catch (updateError) {
      setError(getApiErrorMessage(updateError));
    } finally {
      setLoadingAction(null);
    }
  };

  const handleDeleteItem = async (item: ComandaItem) => {
    if (!selectedComanda) return;

    setLoadingAction(`delete-${item.uuid}`);
    setError("");
    try {
      const updated = await comandasApi.deleteItem(selectedComanda.uuid, item.uuid);
      updateComandaState(updated);
      setProdutos(await produtosApi.list());
    } catch (deleteError) {
      setError(getApiErrorMessage(deleteError));
    } finally {
      setLoadingAction(null);
    }
  };

  const handleCloseComanda = async (status: "PAGA" | "FIADO") => {
    if (!selectedComanda) return;

    setLoadingAction(status === "PAGA" ? "close-paga" : "close-fiado");
    setError("");
    try {
      await comandasApi.close(selectedComanda.uuid, status);
      const nextAbertas = comandasAbertas.filter((comanda) => comanda.uuid !== selectedComanda.uuid);
      const nextFiado = comandasFiado.filter((comanda) => comanda.uuid !== selectedComanda.uuid);
      setComandasAbertas(nextAbertas);
      setComandasFiado(nextFiado);
      setSelectedUuid(nextAbertas[0]?.uuid || nextFiado[0]?.uuid || "");
      setClosingDialog(false);
    } catch (closeError) {
      setError(getApiErrorMessage(closeError));
    } finally {
      setLoadingAction(null);
    }
  };

  const medidaDisponivel = tipoMedida === "UNIDADE" || Boolean(selectedProduto?.valorCaixa);
  const editMedidaDisponivel = editTipoMedida === "UNIDADE" || Boolean(selectedEditProduto?.valorCaixa);
  const selectedIsFiado = selectedComanda?.status === "FIADO";
  const totalPreview =
    selectedProduto && medidaDisponivel
      ? Number(quantidade) *
        Number(tipoMedida === "CAIXA" ? selectedProduto.valorCaixa : selectedProduto.valorUnidade)
      : 0;
  const editTotalPreview =
    selectedEditProduto && editMedidaDisponivel
      ? Number(editQuantidade) *
        Number(editTipoMedida === "CAIXA" ? selectedEditProduto.valorCaixa : selectedEditProduto.valorUnidade)
      : 0;

  return (
    <DashboardLayout>
      <DashboardNavbar />
      <MDBox py={3}>
        <Grid container spacing={3}>
          <Grid item xs={12} lg={4}>
            <Card>
              <MDBox p={3}>
                <MDTypography variant="h5" fontWeight="medium" mb={2}>
                  Abrir comanda
                </MDTypography>
                <MDBox component="form" onSubmit={handleOpenComanda} display="flex" gap={1.5}>
                  <TextField
                    label="Responsável"
                    required
                    fullWidth
                    value={novoResponsavel}
                    onChange={(event) => setNovoResponsavel(event.target.value)}
                  />
                  <MDButton
                    type="submit"
                    variant="gradient"
                    color="info"
                    disabled={actionLoading && loadingAction !== "open"}
                    loading={loadingAction === "open"}
                    loadingText="Abrindo..."
                  >
                    Abrir
                  </MDButton>
                </MDBox>
              </MDBox>
            </Card>

            <MDBox mt={3}>
              <Card>
                <MDBox p={3}>
                  <MDTypography variant="h6" fontWeight="medium" mb={2}>
                    Comandas abertas
                  </MDTypography>
                  <MDBox display="flex" flexDirection="column" gap={1}>
                    {comandasAbertas.map((comanda) => (
                      <MDButton
                        key={comanda.uuid}
                        variant={selectedComanda?.uuid === comanda.uuid ? "contained" : "outlined"}
                        color="info"
                        fullWidth
                        onClick={() => setSelectedUuid(comanda.uuid)}
                        sx={{
                          minHeight: 48,
                          justifyContent: "center",
                          bgcolor: selectedComanda?.uuid === comanda.uuid ? "info.main" : "white",
                          borderColor: selectedComanda?.uuid === comanda.uuid ? "info.main" : "#e5e7eb",
                          color: selectedComanda?.uuid === comanda.uuid ? "#ffffff !important" : "#344767",
                          boxShadow: selectedComanda?.uuid === comanda.uuid ? 2 : "none",
                          "&, & span, & p": {
                            color:
                              selectedComanda?.uuid === comanda.uuid
                                ? "#ffffff !important"
                                : "#344767 !important",
                          },
                          "&:hover": {
                            bgcolor: selectedComanda?.uuid === comanda.uuid ? "info.dark" : "#f8fafc",
                            borderColor: selectedComanda?.uuid === comanda.uuid ? "info.dark" : "#cbd5e1",
                          },
                        }}
                      >
                        {comanda.nomeResponsavel} - {currency.format(Number(comanda.total || 0))}
                      </MDButton>
                    ))}
                    {comandasAbertas.length === 0 && (
                      <MDTypography variant="button" color="text">
                        Nenhuma comanda aberta.
                      </MDTypography>
                    )}
                  </MDBox>
                </MDBox>
              </Card>
            </MDBox>

            <MDBox mt={3}>
              <Card>
                <MDBox p={3}>
                  <MDTypography variant="h6" fontWeight="medium" mb={2}>
                    Comandas no fiado
                  </MDTypography>
                  <MDBox display="flex" flexDirection="column" gap={1}>
                    {comandasFiado.map((comanda) => (
                      <MDButton
                        key={comanda.uuid}
                        variant={selectedComanda?.uuid === comanda.uuid ? "contained" : "outlined"}
                        color="warning"
                        fullWidth
                        onClick={() => setSelectedUuid(comanda.uuid)}
                        sx={{
                          minHeight: 48,
                          justifyContent: "center",
                          bgcolor: selectedComanda?.uuid === comanda.uuid ? "#f59e0b" : "white",
                          borderColor: selectedComanda?.uuid === comanda.uuid ? "#f59e0b" : "#e5e7eb",
                          color: selectedComanda?.uuid === comanda.uuid ? "#ffffff !important" : "#344767",
                          boxShadow: selectedComanda?.uuid === comanda.uuid ? 2 : "none",
                          "&, & span, & p": {
                            color:
                              selectedComanda?.uuid === comanda.uuid
                                ? "#ffffff !important"
                                : "#344767 !important",
                          },
                          "&:hover": {
                            bgcolor: selectedComanda?.uuid === comanda.uuid ? "#d97706" : "#f8fafc",
                            borderColor: selectedComanda?.uuid === comanda.uuid ? "#d97706" : "#cbd5e1",
                          },
                        }}
                      >
                        {comanda.nomeResponsavel} - {currency.format(Number(comanda.total || 0))}
                      </MDButton>
                    ))}
                    {comandasFiado.length === 0 && (
                      <MDTypography variant="button" color="text">
                        Nenhuma comanda no fiado.
                      </MDTypography>
                    )}
                  </MDBox>
                </MDBox>
              </Card>
            </MDBox>
          </Grid>

          <Grid item xs={12} lg={8}>
            <Card>
              <MDBox p={3}>
                <MDBox display="flex" justifyContent="space-between" alignItems="center" gap={2} mb={2}>
                  <MDBox>
                    <MDTypography variant="h5" fontWeight="medium">
                      {selectedComanda?.nomeResponsavel || "Selecione uma comanda"}
                    </MDTypography>
                    {selectedComanda && (
                      <Chip
                        size="small"
                        color={selectedIsFiado ? "warning" : "info"}
                        label={selectedIsFiado ? "Fiado" : "Aberta"}
                      />
                    )}
                  </MDBox>
                  {selectedComanda && (
                    <MDButton
                      variant="gradient"
                      color="success"
                      disabled={actionLoading}
                      onClick={() => setClosingDialog(true)}
                    >
                      {selectedIsFiado ? "Marcar como paga" : "Fechar"}
                    </MDButton>
                  )}
                </MDBox>

                {error && (
                  <Alert severity="error" sx={{ mb: 2 }}>
                    {error}
                  </Alert>
                )}

                {selectedComanda && (
                  <>
                    {!selectedIsFiado && (
                      <MDBox component="form" onSubmit={handleAddItem} mb={3}>
                        <Grid container spacing={2} alignItems="center">
                          <Grid item xs={12} md={5}>
                            <Autocomplete
                              openOnFocus
                              options={produtos}
                              value={selectedProduto || null}
                              getOptionLabel={(produto) =>
                                `${produto.nome} (${produto.quantidadeEstoqueUnidades} un.)`
                              }
                              isOptionEqualToValue={(option, value) => option.uuid === value.uuid}
                              noOptionsText="Nenhum produto encontrado"
                              onChange={(_, produto) => setProdutoUuid(produto?.uuid || "")}
                              renderInput={(params) => (
                                <TextField {...params} label="Produto" required fullWidth />
                              )}
                            />
                          </Grid>
                          <Grid item xs={6} md={2}>
                            <TextField
                              label="Qtd."
                              type="number"
                              required
                              fullWidth
                              inputProps={{ min: 1 }}
                              value={quantidade}
                              onChange={(event) => setQuantidade(Number(event.target.value))}
                            />
                          </Grid>
                          <Grid item xs={12} md={5}>
                            <ToggleButtonGroup
                              color="info"
                              exclusive
                              fullWidth
                              value={tipoMedida}
                              onChange={(_, value) => value && setTipoMedida(value)}
                            >
                              <ToggleButton value="UNIDADE">
                                Unidade{" "}
                                {selectedProduto ? currency.format(Number(selectedProduto.valorUnidade)) : ""}
                              </ToggleButton>
                              <ToggleButton value="CAIXA" disabled={!selectedProduto?.valorCaixa}>
                                Caixa{" "}
                                {selectedProduto?.valorCaixa
                                  ? currency.format(Number(selectedProduto.valorCaixa))
                                  : ""}
                              </ToggleButton>
                            </ToggleButtonGroup>
                          </Grid>
                          <Grid item xs={12}>
                            <MDBox display="flex" justifyContent="space-between" alignItems="center" gap={2}>
                              <MDTypography variant="button" color="text">
                                Prévia: {currency.format(totalPreview)}
                              </MDTypography>
                              <MDButton
                                type="submit"
                                variant="gradient"
                                color="info"
                                disabled={
                                  !produtoUuid ||
                                  !medidaDisponivel ||
                                  (actionLoading && loadingAction !== "add")
                                }
                                loading={loadingAction === "add"}
                                loadingText="Adicionando..."
                              >
                                Adicionar
                              </MDButton>
                            </MDBox>
                          </Grid>
                        </Grid>
                      </MDBox>
                    )}

                    <MDBox display="flex" flexDirection="column" gap={1.5}>
                      {selectedComanda.itens.map((item, index) => (
                        <MDBox
                          key={item.uuid || `${item.produtoUuid}-${index}`}
                          display="flex"
                          justifyContent="space-between"
                          alignItems="center"
                          p={2}
                          borderRadius="lg"
                          sx={{ border: "1px solid #e5e7eb" }}
                        >
                          <MDBox>
                            <MDTypography variant="button" fontWeight="medium">
                              {item.produtoNome}
                            </MDTypography>
                            <MDTypography variant="caption" color="text" display="block">
                              {item.quantidadePedida} {item.tipoMedida.toLowerCase()} -{" "}
                              {item.unidadesDeduzidas} un. baixadas
                            </MDTypography>
                          </MDBox>
                          <MDBox display="flex" alignItems="center" gap={1}>
                            <MDTypography variant="button" fontWeight="medium">
                              {currency.format(Number(item.subtotal))}
                            </MDTypography>
                            {!selectedIsFiado && (
                              <MDBox display="flex" alignItems="center">
                                <Tooltip title="Editar item">
                                  <IconButton
                                    color="info"
                                    size="small"
                                    disabled={actionLoading}
                                    onClick={() => openEditItem(item)}
                                  >
                                    <Icon fontSize="small">edit</Icon>
                                  </IconButton>
                                </Tooltip>
                                <Tooltip title="Excluir item">
                                  <IconButton
                                    color="error"
                                    size="small"
                                    disabled={actionLoading}
                                    onClick={() => handleDeleteItem(item)}
                                  >
                                    {loadingAction === `delete-${item.uuid}` ? (
                                      <CircularProgress color="inherit" size={18} />
                                    ) : (
                                      <Icon fontSize="small">delete</Icon>
                                    )}
                                  </IconButton>
                                </Tooltip>
                              </MDBox>
                            )}
                          </MDBox>
                        </MDBox>
                      ))}
                      {selectedComanda.itens.length === 0 && (
                        <MDTypography variant="button" color="text">
                          Nenhum item adicionado.
                        </MDTypography>
                      )}
                    </MDBox>

                    <MDBox mt={3} display="flex" justifyContent="flex-end">
                      <MDTypography variant="h5" fontWeight="bold">
                        Total {currency.format(Number(selectedComanda.total || 0))}
                      </MDTypography>
                    </MDBox>
                  </>
                )}
              </MDBox>
            </Card>
          </Grid>
        </Grid>
      </MDBox>

      <Dialog
        open={closingDialog}
        onClose={() => !actionLoading && setClosingDialog(false)}
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle>{selectedIsFiado ? "Receber fiado" : "Fechar comanda"}</DialogTitle>
        <DialogContent>
          <MDTypography variant="button" color="text">
            Total: {currency.format(Number(selectedComanda?.total || 0))}
          </MDTypography>
        </DialogContent>
        <DialogActions>
          <MDButton
            variant="text"
            color="secondary"
            disabled={actionLoading}
            onClick={() => setClosingDialog(false)}
          >
            Cancelar
          </MDButton>
          {isGestor && !selectedIsFiado && (
            <MDButton
              variant="contained"
              color="warning"
              disabled={actionLoading && loadingAction !== "close-fiado"}
              loading={loadingAction === "close-fiado"}
              loadingText="Salvando..."
              onClick={() => handleCloseComanda("FIADO")}
              sx={{
                minWidth: 96,
                bgcolor: "#f59e0b",
                color: "#ffffff !important",
                boxShadow: 2,
                "&:hover": {
                  bgcolor: "#d97706",
                },
                "&.Mui-disabled": {
                  bgcolor: "#f3f4f6",
                  color: "#9ca3af !important",
                },
              }}
            >
              Fiado
            </MDButton>
          )}
          <MDButton
            variant="gradient"
            color="success"
            disabled={actionLoading && loadingAction !== "close-paga"}
            loading={loadingAction === "close-paga"}
            loadingText="Salvando..."
            onClick={() => handleCloseComanda("PAGA")}
            sx={{ minWidth: 96 }}
          >
            Paga
          </MDButton>
        </DialogActions>
      </Dialog>

      <Dialog open={Boolean(editingItem)} onClose={() => !actionLoading && closeEditItem()} maxWidth="sm" fullWidth>
        <MDBox component="form" onSubmit={handleUpdateItem}>
          <DialogTitle>Editar item</DialogTitle>
          <DialogContent>
            <Grid container spacing={2} mt={0.5}>
              <Grid item xs={12}>
                <Autocomplete
                  openOnFocus
                  options={produtos}
                  value={selectedEditProduto || null}
                  getOptionLabel={(produto) => `${produto.nome} (${produto.quantidadeEstoqueUnidades} un.)`}
                  isOptionEqualToValue={(option, value) => option.uuid === value.uuid}
                  noOptionsText="Nenhum produto encontrado"
                  onChange={(_, produto) => setEditProdutoUuid(produto?.uuid || "")}
                  renderInput={(params) => (
                    <TextField {...params} label="Produto" required fullWidth />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField
                  label="Qtd."
                  type="number"
                  required
                  fullWidth
                  inputProps={{ min: 1 }}
                  value={editQuantidade}
                  onChange={(event) => setEditQuantidade(Number(event.target.value))}
                />
              </Grid>
              <Grid item xs={12} sm={8}>
                <ToggleButtonGroup
                  color="info"
                  exclusive
                  fullWidth
                  value={editTipoMedida}
                  onChange={(_, value) => value && setEditTipoMedida(value)}
                >
                  <ToggleButton value="UNIDADE">
                    Unidade {selectedEditProduto ? currency.format(Number(selectedEditProduto.valorUnidade)) : ""}
                  </ToggleButton>
                  <ToggleButton value="CAIXA" disabled={!selectedEditProduto?.valorCaixa}>
                    Caixa{" "}
                    {selectedEditProduto?.valorCaixa
                      ? currency.format(Number(selectedEditProduto.valorCaixa))
                      : ""}
                  </ToggleButton>
                </ToggleButtonGroup>
              </Grid>
              <Grid item xs={12}>
                <MDTypography variant="button" color="text">
                  Prévia: {currency.format(editTotalPreview)}
                </MDTypography>
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <MDButton variant="text" color="secondary" disabled={actionLoading} onClick={closeEditItem}>
              Cancelar
            </MDButton>
            <MDButton
              type="submit"
              variant="gradient"
              color="info"
              disabled={
                !editProdutoUuid ||
                !editMedidaDisponivel ||
                (actionLoading && loadingAction !== "update")
              }
              loading={loadingAction === "update"}
              loadingText="Salvando..."
            >
              Salvar
            </MDButton>
          </DialogActions>
        </MDBox>
      </Dialog>
      <Footer />
    </DashboardLayout>
  );
}

export default Comandas;
