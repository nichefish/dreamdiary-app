전제. Python 3.11과 uv(의존성 매니저), 그리고 로컬 MariaDB가 필요합니다. GitHub
1) 서버 설치. 레포를 클론하고 의존성을 잡습니다.
   git clone https://github.com/MariaDB/mcp.git
   cd mcp
   python -m pip install uv
   uv lock
   uv sync
2) .env 작성 (프로젝트 루트에). DreamDiary는 임베딩/벡터 검색이 필요 없으니 임베딩 프로바이더는 끄고 갑니다 — OpenAI 키 같은 거 안 넣어도 됩니다. EMBEDDING_PROVIDER를 설정하지 않으면 벡터 도구들은 비활성화되고 표준 SQL만 동작합니다. GitHub
   DB_HOST=localhost
   DB_USER=dreamdiary_ro
   DB_PASSWORD=...
   DB_PORT=3306
   DB_NAME=dreamdiary
   MCP_READ_ONLY=true
   MCP_MAX_POOL_SIZE=10
3) Claude Desktop 등록. 설정 파일은 Windows면 %APPDATA%\Claude\claude_desktop_config.json, macOS면 ~/Library/Application Support/Claude/claude_desktop_config.json입니다. stdio 방식 등록은 이렇게 합니다: GitHub
   json{
   "mcpServers": {
   "MariaDB_Server": {
   "command": "uv",
   "args": ["--directory", "C:/path/to/mcp/", "run", "server.py"],
   "envFile": "C:/path/to/mcp/.env"
   }
   }
   }
   저장 후 Claude Desktop을 완전히 재시작하면 채팅의 도구 목록에 list_databases, list_tables, get_table_schema, execute_sql 등이 뜹니다.
   이제 구조적으로 짚어야 할 한 가지 — 이건 당신이 코딩 에이전트에 대해 이미 도달한 결론과 정확히 같은 자리입니다.
   .env의 MCP_READ_ONLY=true를 켰다고 안심하면 안 됩니다. 레포 작성자가 직접 명시합니다: READ_ONLY 플래그는 허용 쿼리 화이트리스트에 기반한 최선의 노력일 뿐이고, 진짜 적대적 사용자 앞에서는 적절한 DB 사용자 권한의 대체물이 아니며, 100% 읽기 전용을 확실하게 보장하는 유일한 방법은 MariaDB 사용자 자체를 그에 맞는 권한으로 설정하는 것입니다. GitHub
   그래서 위 .env에 DB_USER를 dreamdiary_ro로 적어둔 겁니다 — SELECT만 가진 전용 계정을 따로 만들라는 뜻입니다:
   sqlCREATE USER 'dreamdiary_ro'@'localhost' IDENTIFIED BY '...';
   GRANT SELECT ON dreamdiary.* TO 'dreamdiary_ro'@'localhost';
   이건 단순 보안 팁이 아니라 당신의 원칙 그 자체의 재현입니다. "AI의 매너에 의존하지 말고 아키텍처에 의존한다." 애플리케이션 레이어의 플래그(MCP_READ_ONLY)는 AI/서버의 매너고, DB 권한(GRANT SELECT)은 아키텍처입니다. REMOVE_CONTENT 필터가 지시 없이 끼어들던 그 경험을 떠올리면 — 모델이 execute_sql로 무엇을 시도하든, 권한 레벨에서 막혀 있으면 의도와 무관하게 쓰기가 불가능합니다. 화이트리스트는 우회될 수 있지만 권한 경계는 우회되지 않습니다.
   DreamDiary 개발 DB 하나라면 사실 위험도는 낮습니다(데이터 민감도 낮음, 본인 소유). 하지만 같은 패턴을 나중에 회사 LIMS DB에 적용할 생각이 조금이라도 있다면, 지금 개발 DB에서 read-only 계정 분리 습관을 들여두는 게 맞습니다. 그쪽은 고객 실험 데이터라 매너에 맡길 수 있는 영역이 전혀 아니니까요.
   막히는 지점 — uv 경로 문제든, 재시작 후 도구가 안 뜨든 — 생기면 그 상태 그대로 가져오세요.
