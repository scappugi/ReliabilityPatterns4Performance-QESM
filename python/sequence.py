
import pandas as pd
import matplotlib.pyplot as plt

# Leggiamo i due CSV
no_retry = pd.read_csv("sequence_no_retry_cdf.csv")
with_retry = pd.read_csv("workflow_cdf.csv")

plt.figure(figsize=(10, 6))

plt.plot(
    no_retry["time"],
    no_retry["cdf"],
    label="Sequenza senza retry",
    linewidth=2
)

plt.plot(
    with_retry["time"],
    with_retry["cdf"],
    label="Sequenza con retry",
    linewidth=2
)

plt.xlabel("Tempo di risposta (secondi)")
plt.ylabel("CDF - Probabilità cumulativa")
plt.title("Confronto sequenza: con e senza retry")

plt.xlim(0, 20)
plt.ylim(0, 1.05)

plt.grid(True, alpha=0.3)
plt.legend()
plt.tight_layout()
plt.show()
