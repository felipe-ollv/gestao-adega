import { FormEvent, useEffect, useMemo, useState } from "react";

import Alert from "@mui/material/Alert";
import Card from "@mui/material/Card";
import Chip from "@mui/material/Chip";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Grid from "@mui/material/Grid";
import MenuItem from "@mui/material/MenuItem";
import TextField from "@mui/material/TextField";
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
  Produto,
  TipoMedidaVenda,
  comandasApi,
  getApiErrorMessage,
  produtosApi,
} from "services/adega";

const currency = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

function Comandas() {
  const { isGestor } = useUser();
  const [comandas, setComandas] = useState<Comanda[]>([]);
  const [produtos, setProdutos] = useState<Produto[]>([]);
  const [selectedUuid, setSelectedUuid] = useState("");
  const [novoResponsavel, setNovoResponsavel] = useState("");
  const [produtoUuid, setProdutoUuid] = useState("");
  const [quantidade, setQuantidade] = useState(1);
  const [tipoMedida, setTipoMedida] = useState<TipoMedidaVenda>("UNIDADE");
  const [closingDialog, setClosingDialog] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const selectedComanda = useMemo(
    () => comandas.find((comanda) => comanda.uuid === selectedUuid) || comandas[0],
    [comandas, selectedUuid]
  );

  const selectedProduto = produtos.find((produto) => produto.uuid === produtoUuid);

  const loadData = async () => {
    setError("");
    try {
      const [comandasData, produtosData] = await Promise.all([
        comandasApi.list("ABERTA"),
        produtosApi.list(),
      ]);
      setComandas(comandasData);
      setProdutos(produtosData);
      if (!selectedUuid && comandasData[0]) {
        setSelectedUuid(comandasData[0].uuid);
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

    setLoading(true);
    setError("");
    try {
      const comanda = await comandasApi.open(novoResponsavel);
      setComandas((current) => [comanda, ...current]);
      setSelectedUuid(comanda.uuid);
      setNovoResponsavel("");
    } catch (openError) {
      setError(getApiErrorMessage(openError));
    } finally {
      setLoading(false);
    }
  };

  const handleAddItem = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!selectedComanda || !produtoUuid) return;

    setLoading(true);
    setError("");
    try {
      const updated = await comandasApi.addItem(selectedComanda.uuid, {
        produtoUuid,
        quantidade: Number(quantidade),
        tipoMedida,
      });
      setComandas((current) =>
        current.map((comanda) => (comanda.uuid === updated.uuid ? updated : comanda))
      );
      setProdutoUuid("");
      setQuantidade(1);
      setTipoMedida("UNIDADE");
      setProdutos(await produtosApi.list());
    } catch (addError) {
      setError(getApiErrorMessage(addError));
    } finally {
      setLoading(false);
    }
  };

  const handleCloseComanda = async (status: "PAGA" | "FIADO") => {
    if (!selectedComanda) return;

    setLoading(true);
    setError("");
    try {
      await comandasApi.close(selectedComanda.uuid, status);
      const next = comandas.filter((comanda) => comanda.uuid !== selectedComanda.uuid);
      setComandas(next);
      setSelectedUuid(next[0]?.uuid || "");
      setClosingDialog(false);
    } catch (closeError) {
      setError(getApiErrorMessage(closeError));
    } finally {
      setLoading(false);
    }
  };

  const medidaDisponivel = tipoMedida === "UNIDADE" || Boolean(selectedProduto?.valorCaixa);
  const totalPreview =
    selectedProduto && medidaDisponivel
      ? Number(quantidade) *
        Number(tipoMedida === "CAIXA" ? selectedProduto.valorCaixa : selectedProduto.valorUnidade)
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
                  <MDButton type="submit" variant="gradient" color="info" disabled={loading}>
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
                    {comandas.map((comanda) => (
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
                    {comandas.length === 0 && (
                      <MDTypography variant="button" color="text">
                        Nenhuma comanda aberta.
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
                    {selectedComanda && <Chip size="small" color="info" label="Aberta" />}
                  </MDBox>
                  {selectedComanda && (
                    <MDButton variant="gradient" color="success" onClick={() => setClosingDialog(true)}>
                      Fechar
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
                    <MDBox component="form" onSubmit={handleAddItem} mb={3}>
                      <Grid container spacing={2} alignItems="center">
                        <Grid item xs={12} md={5}>
                          <TextField
                            select
                            label="Produto"
                            required
                            fullWidth
                            value={produtoUuid}
                            onChange={(event) => setProdutoUuid(event.target.value)}
                            sx={{
                              "& .MuiOutlinedInput-root": {
                                minHeight: 56,
                                alignItems: "center",
                              },
                              "& .MuiSelect-select": {
                                minHeight: "24px !important",
                                display: "flex",
                                alignItems: "center",
                                py: "16.5px",
                              },
                              "& .MuiInputLabel-root": {
                                lineHeight: 1.4375,
                              },
                            }}
                          >
                            {produtos.map((produto) => (
                              <MenuItem key={produto.uuid} value={produto.uuid}>
                                {produto.nome} ({produto.quantidadeEstoqueUnidades} un.)
                              </MenuItem>
                            ))}
                          </TextField>
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
                              disabled={loading || !produtoUuid || !medidaDisponivel}
                            >
                              Adicionar
                            </MDButton>
                          </MDBox>
                        </Grid>
                      </Grid>
                    </MDBox>

                    <MDBox display="flex" flexDirection="column" gap={1.5}>
                      {selectedComanda.itens.map((item, index) => (
                        <MDBox
                          key={`${item.produtoUuid}-${index}`}
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
                          <MDTypography variant="button" fontWeight="medium">
                            {currency.format(Number(item.subtotal))}
                          </MDTypography>
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

      <Dialog open={closingDialog} onClose={() => setClosingDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Fechar comanda</DialogTitle>
        <DialogContent>
          <MDTypography variant="button" color="text">
            Total: {currency.format(Number(selectedComanda?.total || 0))}
          </MDTypography>
        </DialogContent>
        <DialogActions>
          <MDButton variant="text" color="secondary" onClick={() => setClosingDialog(false)}>
            Cancelar
          </MDButton>
          {isGestor && (
            <MDButton variant="outlined" color="warning" disabled={loading} onClick={() => handleCloseComanda("FIADO")}>
              Fiado
            </MDButton>
          )}
          <MDButton variant="gradient" color="success" disabled={loading} onClick={() => handleCloseComanda("PAGA")}>
            Paga
          </MDButton>
        </DialogActions>
      </Dialog>
      <Footer />
    </DashboardLayout>
  );
}

export default Comandas;
