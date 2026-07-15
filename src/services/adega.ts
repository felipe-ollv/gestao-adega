import api from "services/api";

export type PerfilUsuario = "GESTOR" | "ATENDENTE";
export type StatusComanda = "ABERTA" | "PAGA" | "FIADO";
export type TipoMedidaVenda = "UNIDADE" | "CAIXA";

export type AuthResponse = {
  token: string;
  usuarioUuid: string;
  adegaUuid: string;
  nome: string;
  perfil: PerfilUsuario;
};

export type Produto = {
  uuid: string;
  nome: string;
  quantidadeEstoqueUnidades: number;
  unidadesPorCaixa: number;
  valorUnidade: number;
  valorCaixa?: number | null;
};

export type ComandaItem = {
  produtoUuid: string;
  produtoNome: string;
  quantidadePedida: number;
  unidadesDeduzidas: number;
  tipoMedida: TipoMedidaVenda;
  valorUnitario: number;
  subtotal: number;
};

export type Comanda = {
  uuid: string;
  nomeResponsavel: string;
  dataAbertura: string;
  dataFechamento?: string | null;
  status: StatusComanda;
  itens: ComandaItem[];
  total: number;
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
    payload: { produtoUuid: string; quantidade: number; tipoMedida: TipoMedidaVenda }
  ) =>
    api.post<Comanda>(`/comandas/${comandaUuid}/itens`, payload).then((response) => response.data),
  close: (comandaUuid: string, status: Exclude<StatusComanda, "ABERTA">) =>
    api.patch<Comanda>(`/comandas/${comandaUuid}/fechar`, { status }).then((response) => response.data),
};

export const usuariosApi = {
  list: () => api.get<Usuario[]>("/usuarios").then((response) => response.data),
  create: (payload: { nome: string; email: string; senha: string; perfil: PerfilUsuario }) =>
    api.post<Usuario>("/usuarios", payload).then((response) => response.data),
};

export const getApiErrorMessage = (error: any) =>
  error?.response?.data?.error || error?.message || "Não foi possível concluir a operação.";
