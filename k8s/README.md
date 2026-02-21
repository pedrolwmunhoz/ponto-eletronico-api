# Deploy no Kubernetes (ArgoCD + Docker Hub)

## Fluxo

1. Push em `master` → GitHub Actions builda a imagem e envia para **Docker Hub** (`clockintech/ponto-eletronico-api:latest`)
2. ArgoCD detecta alterações em `k8s/` e aplica os manifests no cluster k3s (k8s-node-1)

## Infraestrutura

| Item                | Valor |
|---------------------|-------|
| VM GCP              | `pedro_munhoz_pessoal@34.39.207.249` (k8s-node-1) |
| Kubernetes          | k3s v1.34.4 |
| Ingress Controller  | Traefik (incluso no k3s) |
| ArgoCD              | namespace `argocd`, NodePort 32601 (HTTP) / 30213 (HTTPS) |
| ArgoCD UI           | `http://34.39.207.249:32601` |
| ArgoCD admin        | user: `admin` |
| Docker Hub          | `clockintech/ponto-eletronico-api` |
| GitHub repo         | `pedrolwmunhoz/ponto-eletronico-api` (branch `master`) |

## Secrets configurados

### GitHub Actions (Settings → Secrets)
- `DOCKERHUB_USERNAME` = `clockintech`
- `DOCKERHUB_TOKEN` = Access Token Docker Hub

### Kubernetes (namespace default)
- `ponto-eletronico-api-secret` — credenciais do banco (spring.datasource.*)

## Arquivos

| Arquivo            | Descrição |
|--------------------|-----------|
| `deployment.yaml`  | Deployment (imagem, réplicas, env, probes) |
| `service.yaml`     | Service ClusterIP 80 → 8081 |
| `ingress.yaml`     | Ingress via Traefik |
| `secret.example.yaml` | Referência do formato do Secret |
