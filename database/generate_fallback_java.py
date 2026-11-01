# -*- coding: utf-8 -*-
import sys
import os

from generate_seed_bank import questions

def esc_java(s):
    if s is None:
        return 'null'
    escaped = s.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n').replace('\r', '')
    return '"' + escaped + '"'

lines = []
lines.append('package com.devclash.rmiserver.engine;')
lines.append('')
lines.append('import com.devclash.common.enums.Difficulty;')
lines.append('import java.util.ArrayList;')
lines.append('import java.util.List;')
lines.append('')
lines.append('/**')
lines.append(' * Curated in-memory fallback bank of 96 questions (16 per category).')
lines.append(' * Guarantees zero downtime and independent unit testability without MySQL.')
lines.append(' */')
lines.append('public class FallbackQuestionBank {')
lines.append('')
lines.append('    public static List<FullQuestion> getQuestionsForCategory(int categoryId, int timeLimitSec) {')
lines.append('        List<FullQuestion> list = new ArrayList<>();')
lines.append('        for (FullQuestion q : getAllQuestions(timeLimitSec)) {')
lines.append('            if (q.getCategoryId() == categoryId || categoryId <= 0) {')
lines.append('                list.add(q);')
lines.append('            }')
lines.append('        }')
lines.append('        return list;')
lines.append('    }')
lines.append('')
lines.append('    public static List<FullQuestion> getAllQuestions(int timeLimitSec) {')
lines.append('        List<FullQuestion> all = new ArrayList<>(96);')
lines.append('        int limit = timeLimitSec > 0 ? timeLimitSec : 20;')

for idx, q in enumerate(questions, start=1):
    c_id = q['category_id']
    diff = q['difficulty']
    q_txt = esc_java(q['question_text'])
    snippet = esc_java(q['code_snippet'])
    oa = esc_java(q['option_a'])
    ob = esc_java(q['option_b'])
    oc = esc_java(q['option_c'])
    od = esc_java(q['option_d'])
    corr = esc_java(q['correct_option'])
    exp = esc_java(q['explanation'])
    lines.append(f'        all.add(new FullQuestion({idx}, {c_id}, Difficulty.{diff}, {q_txt}, {snippet}, {oa}, {ob}, {oc}, {od}, {corr}, {exp}, limit));')

lines.append('        return all;')
lines.append('    }')
lines.append('}')
lines.append('')

out_path = r'c:\Users\Sejal Pc\Desktop\DevClash\rmi-server\src\main\java\com\devclash\rmiserver\engine\FallbackQuestionBank.java'
with open(out_path, 'w', encoding='utf-8') as f:
    f.write('\n'.join(lines))
print('Generated FallbackQuestionBank.java successfully with', len(questions), 'questions.')
