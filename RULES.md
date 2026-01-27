# Regras de Detecção de Fraudes em Transações de Criptomoedas

**Instrução Global:**\
Desconsidere todas as regras que dependem de histórico caso o endereço
(`fromAddress`) possua menos de **5 transações registradas**.

------------------------------------------------------------------------

## 1. HIGH_VALUE_AT_NIGHT

-   **Descrição:** Detecta transações de alto valor feitas em horário
    incomum (madrugada).
-   **Campos usados:** `amount`, `realizedAt`
-   **Critério:** `amount` acima do limite definido e horário entre
    00h--05h.

------------------------------------------------------------------------

## 2. NEW_ADDRESS_HIGH_VALUE

-   **Descrição:** Identifica valores altos enviados por carteiras
    novas.
-   **Campos usados:** `fromAddress`, `amount`
-   **Critério:** Endereço com poucas transações anteriores e `amount`
    alto.

------------------------------------------------------------------------

## 3. VALUE_ANOMALY_FROM_HISTORY

⚠ *Depende de histórico; ignorar se \< 5 transações* - **Descrição:**
Verifica se o valor enviado é muito diferente da média histórica da
carteira. - **Campos usados:** `fromAddress`, `amount`

------------------------------------------------------------------------

## 4. STRUCTURED_SMALL_TRANSFERS

⚠ *Depende de histórico; ignorar se \< 5 transações* - **Descrição:**
Detecta comportamento de "smurfing": muitas transações pequenas em
intervalos regulares. - **Campos usados:** `fromAddress`, `amount`,
`realizedAt`

------------------------------------------------------------------------

## 5. BURSTING_MULTIPLE_TRANSACTIONS

⚠ *Depende de histórico; ignorar se \< 5 transações* - **Descrição:**
Verifica se a carteira realizou várias transações em um curto
intervalo. - **Campos usados:** `fromAddress`, `eventTimestamp`

------------------------------------------------------------------------

## 6. FIRST_TIME_TRANSACTION

-   **Descrição:** Detecta se a carteira está realizando sua primeira
    transação.
-   **Campos usados:** `fromAddress`

------------------------------------------------------------------------

## 7. TRANSFER_TO_UNUSUAL_RECIPIENT

⚠ *Depende de histórico; ignorar se \< 5 transações* - **Descrição:**
Identifica transações enviadas para destinatários novos/atípicos. -
**Campos usados:** `fromAddress`, `toAddress`

------------------------------------------------------------------------

## 8. SUSPICIOUS_LABELS

-   **Descrição:** Identifica labels suspeitos ou associados a
    comportamentos maliciosos.
-   **Campos usados:** `fromLabel`, `toLabel`
-   **Critério:** Presença de palavras como "mix", "bot", "tmp", "scam",
    etc.

------------------------------------------------------------------------

## 9. RANDOM_OR_MEANINGLESS_LABEL

-   **Descrição:** Detecta labels sem sentido ou muito curtos.
-   **Campos usados:** `fromLabel`, `toLabel`
-   **Critério:** Ex.: "aaa", "xxx", "test".

------------------------------------------------------------------------

## 10. ADDRESS_IN_BLACKLIST

-   **Descrição:** Verifica se origem ou destino está presente em listas
    externas de risco.
-   **Campos usados:** `fromAddress`, `toAddress`

------------------------------------------------------------------------

## 11. UNUSUAL_OPERATING_HOURS

⚠ *Depende de histórico; ignorar se \< 5 transações* - **Descrição:**
Detecta operação em horários fora do padrão daquele endereço. - **Campos
usados:** `fromAddress`, `realizedAt`

------------------------------------------------------------------------

## 12. PENDING_TOO_LONG

-   **Descrição:** Identifica transações que permanecem em `PENDING` por
    tempo excessivo.
-   **Campos usados:** `status`, `createdAt`

------------------------------------------------------------------------

## 13. SAME_FROM_AND_TO_ADDRESS

-   **Descrição:** Detecta transações onde origem e destino são iguais.
-   **Campos usados:** `fromAddress`, `toAddress`

------------------------------------------------------------------------

### Nota Final

Regras dependentes de histórico só devem ser aplicadas quando a carteira
possuir **5 ou mais transações registradas**.
