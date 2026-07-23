import api from "services/api";

export type PerfilUsuario = "GESTOR" | "ATENDENTE";
export type StatusPagamento = "PENDENTE" | "PAGO";
export type StatusComanda = "ABERTA" | "PAGA" | "FIADO" | "EXCLUIDA";
export type TipoMedidaVenda = "UNIDADE" | "CAIXA";

export type AuthResponse = {
  token: string | null;
  usuarioUuid: string;
  adegaUuid: string;
  adegaNome: string;
  nome: string;
  perfil: PerfilUsuario;
  statusPagamento: StatusPagamento;
  mensalidadePaga: boolean;
  competenciaMensalidade: string;
  vencimentoMensalidade: string;
};

export type Produto = {
  uuid: string;
  nome: string;
  quantidadeEstoqueUnidades: number;
  alertaEstoqueUnidades: number;
  unidadesPorCaixa: number;
  valorUnidade: number;
  valorCaixa?: number | null;
};

export type ComandaItem = {
  uuid: string;
  produtoUuid: string;
  produtoNome: string;
  quantidadePedida: number;
  unidadesDeduzidas: number;
  tipoMedida: TipoMedidaVenda;
  valorUnitario: number;
  subtotal: number;
  grupoUuid?: string | null;
  ordemGrupo?: number | null;
};

export type ComandaItemInput = {
  produtoUuid: string;
  quantidade: number;
  tipoMedida: TipoMedidaVenda;
};

export type Comanda = {
  uuid: string;
  nomeResponsavel: string;
  dataAbertura: string;
  dataFechamento?: string | null;
  dataExclusao?: string | null;
  status: StatusComanda;
  itens: ComandaItem[];
  total: number;
  valorPagoParcial: number;
  saldoPendente: number;
  observacaoExclusao?: string | null;
};

export type Usuario = {
  uuid: string;
  nome: string;
  email: string;
  perfil: PerfilUsuario;
  ativo: boolean;
};

export const authApi = {
  login: (email: string, senha: string) =>
    api.post<AuthResponse>("/auth/login", { email, senha }).then((response) => response.data),
  register: (payload: {
    nomeAdega: string;
    cnpjCpf: string;
    nomeUsuario: string;
    email: string;
    senha: string;
  }) => api.post<AuthResponse>("/auth/register", payload).then((response) => response.data),
};

export const produtosApi = {
  list: () => api.get<Produto[]>("/produtos").then((response) => response.data),
  create: (payload: Omit<Produto, "uuid">) =>
    api.post<Produto>("/produtos", payload).then((response) => response.data),
  update: (uuid: string, payload: Omit<Produto, "uuid">) =>
    api.put<Produto>(`/produtos/${uuid}`, payload).then((response) => response.data),
  delete: (uuid: string) => api.delete(`/produtos/${uuid}`),
};

export const comandasApi = {
  list: (status?: StatusComanda) =>
    api
      .get<Comanda[]>("/comandas", { params: status ? { status } : undefined })
      .then((response) => response.data),
  open: (nomeResponsavel: string) =>
    api.post<Comanda>("/comandas", { nomeResponsavel }).then((response) => response.data),
  addItem: (
    comandaUuid: string,
    payload: ComandaItemInput
  ) =>
    api.post<Comanda>(`/comandas/${comandaUuid}/itens`, payload).then((response) => response.data),
  addItems: (comandaUuid: string, itens: ComandaItemInput[]) =>
    api
      .post<Comanda>(`/comandas/${comandaUuid}/itens/lote`, { itens })
      .then((response) => response.data),
  updateItem: (
    comandaUuid: string,
    itemUuid: string,
    payload: ComandaItemInput
  ) =>
    api.put<Comanda>(`/comandas/${comandaUuid}/itens/${itemUuid}`, payload).then((response) => response.data),
  deleteItem: (comandaUuid: string, itemUuid: string) =>
    api.delete<Comanda>(`/comandas/${comandaUuid}/itens/${itemUuid}`).then((response) => response.data),
  close: (comandaUuid: string, status: Exclude<StatusComanda, "ABERTA" | "EXCLUIDA">) =>
    api.patch<Comanda>(`/comandas/${comandaUuid}/fechar`, { status }).then((response) => response.data),
  payPartial: (comandaUuid: string, valor: number) =>
    api
      .patch<Comanda>(`/comandas/${comandaUuid}/pagamento-parcial`, { valor })
      .then((response) => response.data),
  delete: (comandaUuid: string, observacao: string) =>
    api.delete(`/comandas/${comandaUuid}`, { data: { observacao } }),
};

export const usuariosApi = {
  list: () => api.get<Usuario[]>("/usuarios").then((response) => response.data),
  create: (payload: { nome: string; email: string; senha: string; perfil: PerfilUsuario }) =>
    api.post<Usuario>("/usuarios", payload).then((response) => response.data),
  update: (
    uuid: string,
    payload: { nome: string; email: string; senha?: string; perfil: PerfilUsuario; ativo: boolean }
  ) => api.put<Usuario>(`/usuarios/${uuid}`, payload).then((response) => response.data),
  delete: (uuid: string) => api.delete(`/usuarios/${uuid}`),
};

export const getApiErrorMessage = (error: any) =>
  error?.response?.data?.error || error?.message || "Não foi possível concluir a operação.";
