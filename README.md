# SpeedCool C++26

> Sucessor multiplataforma do SpeedCool Magisk Module.
> Daemon invisível de otimização automática em tempo real para Linux e Windows.

## Sobre

SpeedCool C++26 é um daemon de otimização de sistema que monitora CPU, RAM, I/O,
temperatura, GPU e bateria, e ajusta automaticamente parâmetros do kernel/sistema
para máxima performance ou economia de energia — sem intervenção do usuário.

### Perfis

| Perfil | CPU Governor | I/O Scheduler | GPU Governor | Swappiness | Uso típico |
|--------|-------------|---------------|--------------|------------|------------|
| Eco | powersave | bfq | powersave | 10 | Baixa carga, notebook |
| Balanced | schedutil | bfq | ondemand | 60 | Uso diário |
| Performance | performance | kyber | performance | 100 | Compilação, render |
| Gaming | performance | none | performance | 100 | Jogos |
| Custom | — | — | — | — | Configurável |

## Stack Tecnológica

| Componente | Tecnologia |
|------------|-----------|
| **Linguagem** | C++26 (`-std=c++26`) |
| **Build** | CMake 4.0 + Ninja |
| **Features** | Contracts (`-fcontracts`), `std::print`, `std::expected` |
| **Logging** | spdlog + `std::format` |
| **Config** | TOML (toml11) |
| **TUI** | FTXUI |
| **Telemetry** | JSON (nlohmann) |
| **Linux** | sysfs, cgroups, /proc, udev |
| **Windows** | Win32, PDH, WMI, Power API |

## Build & Instalação

### Linux

```bash
# Dependências
sudo apt install g++ cmake ninja-build libspdlog-dev libfmt-dev \
  libtoml11-dev libftxui-dev nlohmann-json3-dev pkg-config

# Build
cmake -B build -G Ninja -DCMAKE_BUILD_TYPE=Release
cmake --build build -j$(nproc)

# Instalação (root)
sudo ./scripts/install.sh
```

### Windows

```powershell
# Build com MSVC + vcpkg
cmake -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build --config Release
.\scripts\install.ps1
```

## Uso

```bash
speedcool status                    # Status do sistema
speedcool perf set gaming           # Força perfil
speedcool perf auto on              # Adaptive engine ON
speedcool monitor                   # Dashboard TUI
speedcool history                   # Histórico 24h
speedcool update check              # Verificar atualizações
speedcool update apply              # Aplicar atualização
```

## Testes

```bash
cmake --build build -j$(nproc) && cd build
for t in test_cpu test_memory test_config test_engine test_profile; do
  ./$t && echo "$t: PASS" || echo "$t: FAIL"
done
sudo ./test_daemon_integration  # Testes de integração
```

## Arquitetura

```
src/
├── core/           # Daemon lifecycle, engine adaptativo, perfil manager
├── pal/            # Platform Abstraction Layer (CPU, RAM, I/O, Thermal, GPU, Power)
├── monitor/        # Coleta de métricas (/proc, sysfs)
├── cli/            # CLI + TUI (FTXUI)
├── update/         # Auto-updater via GitHub Releases
└── common/         # Types, error handling, logging
```

## Licença

GPLv3 — herdado do SpeedCool Magisk Module original.

---

**Créditos:** Llucs
