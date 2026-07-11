$(function () {
    const $projectSelect = $('#projectSelect');
    const $startBtn = $('#startTestBtn');
    const $submitBtn = $('#submitTestBtn');
    const $resetBtn = $('#resetTestBtn');
    const $questionList = $('#questionList');
    const $statusBar = $('#statusBar');
    const $resultPanel = $('#resultPanel');
    const $testWorkspace = $('#testWorkspace');
    const $testIntro = $('#testIntro');

    let testStarted = false;

    const questionCount = 10;

    function setStatus(message, type) {
        $statusBar
            .removeClass('info success danger')
            .addClass(type || 'info')
            .text(message);
    }

    function escapeHtml(value) {
        return String(value || '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    function renderQuestions(questions) {
        if (!questions || questions.length === 0) {
            $questionList.empty();
            $submitBtn.prop('disabled', true);
            setStatus('当前题库没有可用题目。', 'danger');
            return;
        }

        const choiceQuestions = questions.filter(function (item) {
            return item.questionType === 'choice';
        });
        const fillQuestions = questions.filter(function (item) {
            return item.questionType === 'fill';
        });

        const choiceHtml = choiceQuestions.map(function (item, index) {
            return `
                <div class="question-item" data-question-id="${escapeHtml(item.questionId)}" data-question-type="${escapeHtml(item.questionType)}" data-word="${escapeHtml(item.word)}">
                    <div class="question-head">
                        <span class="question-index">${index + 1}</span>
                        <div>
                            <div class="question-word">${escapeHtml(item.prompt)}</div>
                            <div class="question-tip">选择正确翻译</div>
                        </div>
                    </div>
                    <div class="choice-options">
                        ${(item.options || []).map(function (option, optionIndex) {
                            return `
                                <label class="choice-option">
                                    <input type="radio" name="${escapeHtml(item.questionId)}" value="${escapeHtml(option)}">
                                    <span>${escapeHtml(option)}</span>
                                </label>
                            `;
                        }).join('')}
                    </div>
                </div>
            `;
        }).join('');

        const fillHtml = fillQuestions.map(function (item, index) {
            return `
                <div class="question-item" data-question-id="${escapeHtml(item.questionId)}" data-question-type="${escapeHtml(item.questionType)}" data-word="${escapeHtml(item.word)}">
                    <div class="question-head">
                        <span class="question-index">${choiceQuestions.length + index + 1}</span>
                        <div>
                            <div class="question-word">${escapeHtml(item.prompt)}</div>
                            <div class="question-tip">输入对应单词</div>
                        </div>
                    </div>
                    <input type="text" class="input answer-input" placeholder="在这里输入单词">
                </div>
            `;
        }).join('');

        const sections = [];
        if (choiceHtml) {
            sections.push(`
                <section class="question-section">
                    <h3>选择题</h3>
                    <div class="question-list-inner">${choiceHtml}</div>
                </section>
            `);
        }
        if (fillHtml) {
            sections.push(`
                <section class="question-section">
                    <h3>填空题</h3>
                    <div class="question-list-inner">${fillHtml}</div>
                </section>
            `);
        }

        $questionList.html(sections.join(''));
        $submitBtn.prop('disabled', false);
        setStatus(`已加载 ${questions.length} 道题目，开始作答吧。`, 'success');
        $resultPanel.hide().empty();
    }

    function loadQuestions() {
        const project = $projectSelect.val();
        setStatus('正在加载题目...', 'info');
        $submitBtn.prop('disabled', true);

        $.getJSON('/test/questions', {project: project, count: questionCount})
            .done(function (response) {
                if (response.code === 200) {
                    renderQuestions(response.data);
                    return;
                }

                $questionList.empty();
                setStatus(response.message || '加载失败', 'danger');
            })
            .fail(function () {
                $questionList.empty();
                setStatus('题目加载失败，请稍后重试。', 'danger');
            });
    }

    function collectAnswers() {
        const answers = [];
        $questionList.find('.question-item').each(function () {
            const $item = $(this);
            const questionType = $item.data('question-type');
            let answer = '';

            if (questionType === 'choice') {
                answer = $item.find('input[type="radio"]:checked').val() || '';
            } else {
                answer = $item.find('.answer-input').val();
            }

            answers.push({
                questionId: $item.data('question-id'),
                questionType: questionType,
                word: $item.data('word'),
                answer: answer
            });
        });
        return answers;
    }

    function renderResult(response) {
        const data = response.data || {};
        if (data.redirectUrl) {
            window.location.href = data.redirectUrl;
            return;
        }

        const wrongCount = (data.totalCount || 0) - (data.correctCount || 0);
        const html = `
            <div class="result-summary">
                <div>
                    <span class="result-number">${data.correctCount || 0}</span>
                    <span class="result-label">正确</span>
                </div>
                <div>
                    <span class="result-number">${wrongCount}</span>
                    <span class="result-label">错误</span>
                </div>
                <div>
                    <span class="result-number">${data.savedCount || 0}</span>
                    <span class="result-label">已保存</span>
                </div>
            </div>
            <p class="result-message">${escapeHtml(response.message || '测试数据已保存')}</p>
        `;

        $resultPanel.html(html).show();
    }

    function submitTest() {
        const answers = collectAnswers();
        if (answers.length === 0) {
            setStatus('请先加载测试题目。', 'danger');
            return;
        }

        $.ajax({
            url: '/test/submit',
            method: 'POST',
            contentType: 'application/json; charset=UTF-8',
            data: JSON.stringify({
                project: $projectSelect.val(),
                answers: answers
            })
        }).done(function (response) {
            if (response.code === 200) {
                setStatus(response.message || '保存成功', 'success');
                renderResult(response);
                return;
            }

            setStatus(response.message || '提交失败', 'danger');
        }).fail(function () {
            setStatus('提交失败，请稍后重试。', 'danger');
        });
    }

    $startBtn.on('click', function () {
        testStarted = true;
        $testWorkspace.show();
        $testIntro.hide();
        loadQuestions();
    });
    $projectSelect.on('change', function () {
        if (testStarted) {
            loadQuestions();
        }
    });
    $submitBtn.on('click', submitTest);
    $resetBtn.on('click', function () {
        testStarted = true;
        $testWorkspace.show();
        $testIntro.hide();
        loadQuestions();
    });
});