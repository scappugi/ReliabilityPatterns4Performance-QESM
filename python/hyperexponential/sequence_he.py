
import pandas as pd
import matplotlib.pyplot as plt

no_retry = pd.read_csv("sequential_hyperexponential_no_retry_cdf.csv")
with_retry = pd.read_csv("sequential_hyperexponential_retry_cdf.csv")

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
plt.title("Confronto Sequenza di iperesponenziali: con e senza retry")
plt.ylim(0, 1.05)
plt.grid(True, alpha=0.3)
plt.legend()
plt.tight_layout()
plt.show()
