const defaultWhatsappNumber = "5511977648119";

export const paymentWhatsappNumber =
  import.meta.env.VITE_PAYMENT_WHATSAPP_NUMBER || defaultWhatsappNumber;

export const paymentWhatsappMessage =
  import.meta.env.VITE_PAYMENT_WHATSAPP_MESSAGE ||
  "Olá, minha mensalidade está pendente na Gestão Comércio e gostaria de regularizar o acesso.";

const paymentWhatsappDigits = paymentWhatsappNumber.replace(/\D/g, "");

export const paymentWhatsappUrl = paymentWhatsappDigits
  ? `https://wa.me/${paymentWhatsappDigits}?text=${encodeURIComponent(paymentWhatsappMessage)}`
  : "";
