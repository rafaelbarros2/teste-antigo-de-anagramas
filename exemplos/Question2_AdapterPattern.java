package exemplos;

import java.time.LocalDateTime;
import java.util.List;

/**
 * QUESTÃO 2: Design Pattern - Adapter/Wrapper para Desacoplar de Bibliotecas Terceiras
 * 
 * CENÁRIO: Sistema de envio de e-mails que atualmente usa SendGrid, mas pode
 * precisar trocar para AWS SES, Mailgun ou outro provedor no futuro.
 * 
 * PADRÃO ESCOLHIDO: Adapter Pattern (Wrapper)
 * 
 * VANTAGENS:
 * 1. Desacoplamento total do código de negócio da biblioteca específica
 * 2. Facilita testes (mock da interface, não da lib terceira)
 * 3. Troca de provedor sem impacto no código cliente
 * 4. Camada de abstração com vocabulário do domínio
 * 5. Possibilidade de migração gradual (estratégia)
 * 
 * LIMITAÇÕES:
 * 1. Camada adicional de código (overhead mínimo)
 * 2. Features específicas da lib podem ser perdidas na abstração
 * 3. Necessita manutenção de dois contratos (interface + implementação)
 * 4. Pode haver duplicação de modelos (DTOs da lib vs. domínio)
 */
public class Question2_AdapterPattern {

    public static void main(String[] args) {
        // Cliente usa apenas a interface, não conhece SendGrid
        EmailService emailService = EmailServiceFactory.createEmailService();
        
        EmailMessage message = new EmailMessage(
            "user@example.com",
            "Bem-vindo!",
            "Olá, obrigado por se cadastrar."
        );
        
        boolean sent = emailService.sendEmail(message);
        System.out.println("E-mail enviado: " + sent);
        
        // Se mudar de SendGrid para AWS SES, apenas troca a factory
        // O código cliente permanece INALTERADO
    }
}

// ====================================
// 1. CAMADA DE DOMÍNIO (Independente)
// ====================================

/**
 * Interface do domínio - define o contrato usando vocabulário do negócio.
 * NÃO menciona SendGrid, AWS, ou qualquer biblioteca específica.
 */
interface EmailService {
    boolean sendEmail(EmailMessage message);
    boolean sendBulkEmail(List<EmailMessage> messages);
    EmailSendResult sendWithTracking(EmailMessage message);
}

/**
 * Modelo do domínio - representa e-mail no contexto do negócio.
 */
class EmailMessage {
    private final String to;
    private final String subject;
    private final String body;
    private String from;
    private List<String> cc;
    
    public EmailMessage(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }
    
    // Getters
    public String getTo() { return to; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
}

class EmailSendResult {
    private final boolean success;
    private final String messageId;
    private final LocalDateTime sentAt;
    
    public EmailSendResult(boolean success, String messageId) {
        this.success = success;
        this.messageId = messageId;
        this.sentAt = LocalDateTime.now();
    }
    
    public boolean isSuccess() { return success; }
    public String getMessageId() { return messageId; }
}

// ====================================
// 2. ADAPTER para SendGrid (Biblioteca Atual)
// ====================================

/**
 * Adapter que encapsula a biblioteca SendGrid.
 * Traduz chamadas da interface do domínio para API do SendGrid.
 */
class SendGridEmailAdapter implements EmailService {
    // private final SendGrid sendGridClient; // Biblioteca real comentada
    private final String apiKey;
    
    public SendGridEmailAdapter(String apiKey) {
        this.apiKey = apiKey;
        // this.sendGridClient = new SendGrid(apiKey);
    }
    
    @Override
    public boolean sendEmail(EmailMessage message) {
        try {
            // Tradução: Domínio → SendGrid
            // Email email = new Email();
            // email.addTo(message.getTo());
            // email.setSubject(message.getSubject());
            // email.setContent(new Content("text/plain", message.getBody()));
            // 
            // Request request = new Request();
            // request.setMethod(Method.POST);
            // request.setEndpoint("mail/send");
            // request.setBody(email.build());
            // Response response = sendGridClient.api(request);
            // 
            // return response.getStatusCode() == 202;
            
            // Simulação
            System.out.println("[SendGrid] Enviando e-mail para: " + message.getTo());
            return true;
            
        } catch (Exception e) {
            // Log e tratamento de erro
            System.err.println("Erro ao enviar via SendGrid: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean sendBulkEmail(List<EmailMessage> messages) {
        // Implementação usando API de bulk do SendGrid
        return messages.stream().allMatch(this::sendEmail);
    }
    
    @Override
    public EmailSendResult sendWithTracking(EmailMessage message) {
        boolean success = sendEmail(message);
        String messageId = success ? "sg_" + System.currentTimeMillis() : null;
        return new EmailSendResult(success, messageId);
    }
}

// ====================================
// 3. ADAPTER para AWS SES (Biblioteca Alternativa)
// ====================================

/**
 * Adapter alternativo para AWS SES.
 * MESMO código cliente, DIFERENTE implementação.
 */
class AwsSesEmailAdapter implements EmailService {
    // private final AmazonSimpleEmailService sesClient; // Biblioteca real
    private final String region;
    
    public AwsSesEmailAdapter(String region) {
        this.region = region;
        // AmazonSimpleEmailServiceClientBuilder.standard()
        //     .withRegion(region)
        //     .build();
    }
    
    @Override
    public boolean sendEmail(EmailMessage message) {
        try {
            // Tradução: Domínio → AWS SES
            // SendEmailRequest request = new SendEmailRequest()
            //     .withDestination(new Destination().withToAddresses(message.getTo()))
            //     .withMessage(new Message()
            //         .withSubject(new Content().withData(message.getSubject()))
            //         .withBody(new Body().withText(new Content().withData(message.getBody()))))
            //     .withSource(message.getFrom());
            // 
            // SendEmailResult result = sesClient.sendEmail(request);
            // return result.getMessageId() != null;
            
            // Simulação
            System.out.println("[AWS SES] Enviando e-mail para: " + message.getTo());
            return true;
            
        } catch (Exception e) {
            System.err.println("Erro ao enviar via AWS SES: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean sendBulkEmail(List<EmailMessage> messages) {
        // AWS SES tem API específica de bulk
        return messages.stream().allMatch(this::sendEmail);
    }
    
    @Override
    public EmailSendResult sendWithTracking(EmailMessage message) {
        boolean success = sendEmail(message);
        String messageId = success ? "ses_" + System.currentTimeMillis() : null;
        return new EmailSendResult(success, messageId);
    }
}

// ====================================
// 4. FACTORY para Criação
// ====================================

/**
 * Factory que decide qual adapter usar baseado em configuração.
 * Ponto único de mudança ao trocar de provedor.
 */
class EmailServiceFactory {
    
    public static EmailService createEmailService() {
        // Lê de configuração (application.properties, variável de ambiente, etc.)
        String provider = System.getProperty("email.provider", "sendgrid");
        
        switch (provider.toLowerCase()) {
            case "sendgrid":
                return new SendGridEmailAdapter(getApiKey("sendgrid"));
            case "aws-ses":
                return new AwsSesEmailAdapter(getRegion());
            default:
                throw new IllegalArgumentException("Provedor desconhecido: " + provider);
        }
    }
    
    private static String getApiKey(String provider) {
        // Lê de variável de ambiente ou cofre de secrets
        return System.getenv("SENDGRID_API_KEY");
    }
    
    private static String getRegion() {
        return System.getenv("AWS_REGION");
    }
}

/**
 * TÉCNICAS COMPLEMENTARES:
 * 
 * 1. STRATEGY PATTERN: Permitir troca de provedor em runtime
 * 2. CIRCUIT BREAKER: Fallback se provedor principal falhar
 * 3. DECORATOR: Adicionar logging, retry, rate limiting sem mudar adapter
 * 4. FACADE: Simplificar APIs complexas de bibliotecas terceiras
 * 5. ANTI-CORRUPTION LAYER (DDD): Proteger domínio de modelos externos
 * 
 * EXEMPLO DE TESTE (facilitado pelo desacoplamento):
 * 
 * class EmailServiceTest {
 *     @Test
 *     void testSendEmail() {
 *         EmailService mockService = mock(EmailService.class);
 *         when(mockService.sendEmail(any())).thenReturn(true);
 *         
 *         // Testa lógica de negócio SEM dependência real do SendGrid
 *     }
 * }
 */
