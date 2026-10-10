# ⚡ n8n Workflow Catalog

A structured index of the automation projects inside this repository.

> Status labels indicate maturity. Always review credentials, API limits and production behavior before deployment.

## 🤖 AI Agents & AI Workflows

| Project | Focus | Status |
|---|---|---|
| [FAOUZI AI LAB](agents-ia/faouzi-ai-lab/) | Agent safety, evaluation & infrastructure | 🟡 Research / MVP track |
| [Multi-LLM Router](agents-ia/multi-llm-router/) | Groq, Mistral, GLM, Gemini fallback routing | 🟢 Usable |
| [AI Marketing & Sales Director](agents-ia/directeur-marketing-ventes/) | Strategy, content, sales automation | 🟢 Usable |
| [AI Prospecting](agents-ia/prospection-ia/) | Prospect qualification & Gmail drafts | 🟢 Usable |
| [SEO Product Agent](agents-ia/seo-fiches-produits/) | SEO & product descriptions | 🟠 Beta |
| [Customer Conversion & Follow-up](agents-ia/conversion-relance-clients/) | Conversion & reactivation | 🟠 Beta |

## 💼 Business

- [Client record → Google Sheets + Slack](business/fiche-client-google-sheets-slack/)
- [Lead qualification by email](business/qualification-leads-email/)
- [Invoice reminder automation](business/rappel-factures-impayees/)
- [Invoices & quotes → Google Drive](business/sauvegarde-factures-devis-google-drive/)

## 📣 Marketing

- [Customer review collection](marketing/collecte-avis-clients/)
- [Facebook local weather + AI](marketing/facebook-meteo-ia/)
- [Facebook Posts + Reels](marketing/facebook-posts-reels/)
- [LinkedIn from Google Sheets](marketing/linkedin-google-sheets/)
- [Google Business review replies](marketing/reponse-avis-google-business/)
- [Brand monitoring → Google Sheets](marketing/veille-marque-twitter-google-sheets/)
- [TikTok prospecting Luxembourg](marketing/prospection-tiktok-luxembourg/)

## 🛒 E-commerce

- [Amazon price-drop alert](ecommerce/alerte-baisse-prix-amazon/)
- [Google Sheets stock alert](ecommerce/alerte-rupture-stock-google-sheets/)

## 👥 HR

- [Automatic application acknowledgement](rh/accuse-reception-candidature/)
- [CV processing → Notion](rh/tri-cv-email-notion/)

## ✂️ Service businesses

- [Appointment confirmation by SMS](salon/confirmation-rdv-sms/)
- [90-day inactive customer follow-up](salon/relance-clients-inactifs-90j/)

## ⚡ Productivity

- [Public tender AI alerts](productivity/appels-offres-ia-email/)
- [Zoom summary → Notion](productivity/zoom-notion-prompt/)

---

## Status legend

| Status | Meaning |
|---|---|
| 🟢 **Usable** | Documented and suitable for controlled testing |
| 🟡 **Research / MVP** | Specification or implementation in progress |
| 🟠 **Beta** | Requires adaptation and final validation |
| 🆓 **Free demo** | Public example intended for discovery/testing |

## Security note

Never publish API keys, OAuth tokens, passwords, customer data or n8n credentials. Use n8n Credentials and environment variables.

➡️ [Security policy](SECURITY.md)
