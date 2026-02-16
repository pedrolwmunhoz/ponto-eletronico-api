const fs = require("fs");
const j = JSON.parse(fs.readFileSync("feriados.json", "utf8"));
const ent = Object.entries(j).sort((a, b) => a[0].localeCompare(b[0]));
const esc = (s) => s.replace(/'/g, "''");
const vals = ent.map(([d, desc]) => "    ('" + d + "'::date, '" + esc(desc) + "', 1)").join(",\n");
const sql = `-- 945 feriados (2001 a 2078). usuario_id: ADMIN ou primeiro user. tipo_feriado_id=1 NACIONAL
INSERT INTO feriado (id, data, descricao, tipo_feriado_id, usuario_id, ativo, created_at)
SELECT gen_random_uuid(), d.data, d.descricao, d.tipo_feriado_id,
  COALESCE((SELECT u.id FROM users u INNER JOIN tipo_usuario tp ON u.tipo_usuario_id = tp.id WHERE tp.descricao = 'ADMIN' LIMIT 1),(SELECT id FROM users ORDER BY created_at LIMIT 1)),
  true, CURRENT_TIMESTAMP
FROM (VALUES
${vals}
) AS d(data, descricao, tipo_feriado_id);
`;
fs.writeFileSync("insert-feriados-admin.sql", sql, "utf8");
