import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";

import Card from "@mui/material/Card";
import Chip from "@mui/material/Chip";
import Grid from "@mui/material/Grid";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";

import MDBox from "components/MDBox";
import MDButton from "components/MDButton";
import MDTypography from "components/MDTypography";
import DashboardLayout from "examples/LayoutContainers/DashboardLayout";
import DashboardNavbar from "examples/Navbars/DashboardNavbar";
import Footer from "examples/Footer";
import { Comanda, Produto, comandasApi, getApiErrorMessage, produtosApi } from "services/adega";

const currency = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

function StatCard({ label, value, helper }: { label: string; value: string | number; helper: string }) {
  return (
    <Card sx={{ height: "100%" }}>
      <MDBox p={3}>
        <MDTypography variant="button" color="text" fontWeight="medium">
          {label}
        </MDTypography>
        <MDTypography variant="h3" fontWeight="bold">
          {value}
        </MDTypography>
        <MDTypography variant="caption" color="text">
          {helper}
        </MDTypography>
      </MDBox>
    </Card>
  );
}

function Home() {
  const [produtos, setProdutos] = useState<Produto[]>([]);
  const [comandas, setComandas] = useState<Comanda[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError("");
      try {
        const [produtosData, comandasData] = await Promise.all([
          produtosApi.list(),
          comandasApi.list(),
        ]);
        setProdutos(produtosData);
        setComandas(comandasData);
      } catch (fetchError) {
        setError(getApiErrorMessage(fetchError));
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const metrics = useMemo(() => {
    const abertas = comandas.filter((comanda) => comanda.status === "ABERTA");
    const fiado = comandas.filter((comanda) => comanda.status === "FIADO");
    const faturamento = comandas
      .filter((comanda) => comanda.status === "PAGA")
      .reduce((acc, comanda) => acc + Number(comanda.total || 0), 0);
    const baixoEstoque = produtos.filter((produto) => produto.quantidadeEstoqueUnidades <= 12);

    return { abertas, fiado, faturamento, baixoEstoque };
  }, [comandas, produtos]);

  return (
    <DashboardLayout>
      <DashboardNavbar />
      <MDBox py={3}>
        <MDBox mb={3} display="flex" justifyContent="space-between" alignItems="center" gap={2}>
          <MDBox>
            <MDTypography variant="h4" fontWeight="medium">
              Dashboard
            </MDTypography>
            <MDTypography variant="button" color="text">
              Visão operacional da adega.
            </MDTypography>
          </MDBox>
          <MDButton component={Link} to="/comandas" variant="gradient" color="info">
            Abrir comanda
          </MDButton>
        </MDBox>

        {error && (
          <MDBox mb={2}>
            <Chip color="error" label={error} />
          </MDBox>
        )}

        <Grid container spacing={3}>
          <Grid item xs={12} md={3}>
            <StatCard label="Comandas abertas" value={metrics.abertas.length} helper="Em atendimento" />
          </Grid>
          <Grid item xs={12} md={3}>
            <StatCard label="Fiado" value={metrics.fiado.length} helper="Pendências de cobrança" />
          </Grid>
          <Grid item xs={12} md={3}>
            <StatCard label="Faturado" value={currency.format(metrics.faturamento)} helper="Comandas pagas" />
          </Grid>
          <Grid item xs={12} md={3}>
            <StatCard label="Baixo estoque" value={metrics.baixoEstoque.length} helper="Produtos com até 12 un." />
          </Grid>

          <Grid item xs={12} lg={7}>
            <Card>
              <MDBox p={3}>
                <MDTypography variant="h6" fontWeight="medium">
                  Comandas abertas
                </MDTypography>
              </MDBox>
              <TableContainer>
                <Table>
                  <TableHead sx={{ display: "table-header-group" }}>
                    <TableRow>
                      <TableCell>Responsável</TableCell>
                      <TableCell>Itens</TableCell>
                      <TableCell>Total</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {loading && (
                      <TableRow>
                        <TableCell colSpan={3}>Carregando...</TableCell>
                      </TableRow>
                    )}
                    {!loading &&
                      metrics.abertas.map((comanda) => (
                        <TableRow key={comanda.uuid}>
                          <TableCell>{comanda.nomeResponsavel}</TableCell>
                          <TableCell>{comanda.itens.length}</TableCell>
                          <TableCell>{currency.format(Number(comanda.total || 0))}</TableCell>
                        </TableRow>
                      ))}
                    {!loading && metrics.abertas.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={3}>Nenhuma comanda aberta.</TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </Card>
          </Grid>

          <Grid item xs={12} lg={5}>
            <Card>
              <MDBox p={3}>
                <MDTypography variant="h6" fontWeight="medium">
                  Produtos com baixo estoque
                </MDTypography>
              </MDBox>
              <TableContainer>
                <Table>
                  <TableHead sx={{ display: "table-header-group" }}>
                    <TableRow>
                      <TableCell>Produto</TableCell>
                      <TableCell>Unidades</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {metrics.baixoEstoque.map((produto) => (
                      <TableRow key={produto.uuid}>
                        <TableCell>{produto.nome}</TableCell>
                        <TableCell>{produto.quantidadeEstoqueUnidades}</TableCell>
                      </TableRow>
                    ))}
                    {!loading && metrics.baixoEstoque.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={2}>Estoque sem alertas.</TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </Card>
          </Grid>
        </Grid>
      </MDBox>
      <Footer />
    </DashboardLayout>
  );
}

export default Home;
