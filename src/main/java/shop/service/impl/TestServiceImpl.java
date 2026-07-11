package shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import shop.dto.TestAnswerRequest;
import shop.dto.TestQuestionResponse;
import shop.dto.TestSubmitResponse;
import shop.dto.TestWrongWordResponse;
import shop.dto.WrongWordBookResponse;
import shop.entity.Cet4Word;
import shop.entity.Cet6Word;
import shop.entity.TestRecord;
import shop.mapper.Cet4Mapper;
import shop.mapper.Cet6Mapper;
import shop.mapper.TestRecordMapper;
import shop.service.TestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class TestServiceImpl extends ServiceImpl<TestRecordMapper, TestRecord> implements TestService {

    private static final int DEFAULT_QUESTION_COUNT = 10;

    private final Cet4Mapper cet4Mapper;
    private final Cet6Mapper cet6Mapper;

    public TestServiceImpl(Cet4Mapper cet4Mapper, Cet6Mapper cet6Mapper) {
        this.cet4Mapper = cet4Mapper;
        this.cet6Mapper = cet6Mapper;
    }

    @Override
    public List<TestQuestionResponse> generateQuestions(String project, int count) {
        List<WordItem> items = loadWordItems(project);
        if (items.isEmpty()) {
            throw new RuntimeException("当前测试项目没有可用题目");
        }

        int targetCount = count > 0 ? count : DEFAULT_QUESTION_COUNT;
        int choiceCount = Math.max(1, targetCount / 2);
        int fillCount = Math.max(1, targetCount - choiceCount);
        List<WordItem> shuffled = new ArrayList<>(items);
        Collections.shuffle(shuffled, ThreadLocalRandom.current());

        List<WordItem> choiceItems = shuffled.stream().limit(choiceCount).collect(Collectors.toCollection(ArrayList::new));
        List<WordItem> fillItems = shuffled.stream().skip(choiceCount).limit(fillCount).collect(Collectors.toCollection(ArrayList::new));

        if (fillItems.isEmpty() && shuffled.size() > choiceItems.size()) {
            fillItems = shuffled.stream().skip(choiceItems.size()).limit(fillCount).collect(Collectors.toCollection(ArrayList::new));
        }

        List<String> allTranslations = items.stream().map(WordItem::translation).distinct().collect(Collectors.toCollection(ArrayList::new));
        List<TestQuestionResponse> questions = new ArrayList<>();

        for (int i = 0; i < choiceItems.size(); i++) {
            WordItem item = choiceItems.get(i);
            TestQuestionResponse response = new TestQuestionResponse();
            response.setQuestionId("choice-" + (i + 1));
            response.setQuestionType("choice");
            response.setWord(item.word());
            response.setPrompt("选择下列单词的正确翻译：" + item.word());
            response.setOptions(buildChoiceOptions(item.translation(), allTranslations));
            questions.add(response);
        }

        for (int i = 0; i < fillItems.size(); i++) {
            WordItem item = fillItems.get(i);
            TestQuestionResponse response = new TestQuestionResponse();
            response.setQuestionId("fill-" + (i + 1));
            response.setQuestionType("fill");
            response.setWord(item.word());
            response.setPrompt("根据翻译写出对应单词：" + item.translation());
            response.setOptions(Collections.emptyList());
            questions.add(response);
        }

        return questions;
    }

    @Override
    public TestSubmitResponse submit(String project, Long userId, List<TestAnswerRequest> answers) {
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        if (!StringUtils.hasText(project)) {
            throw new RuntimeException("请选择测试项目");
        }
        if (answers == null || answers.isEmpty()) {
            throw new RuntimeException("没有可保存的测试数据");
        }

        int correctCount = 0;
        List<TestRecord> records = new ArrayList<>();
        List<TestWrongWordResponse> wrongWords = new ArrayList<>();

        for (TestAnswerRequest answerRequest : answers) {
            if (answerRequest == null || !StringUtils.hasText(answerRequest.getWord())) {
                continue;
            }

            String word = answerRequest.getWord().trim();
            String questionType = normalizeQuestionType(answerRequest.getQuestionType());
            String userAnswer = answerRequest.getAnswer();
            String correctTranslation = resolveTranslation(project, word);
            boolean correct = isCorrectAnswer(questionType, userAnswer, word, correctTranslation);

            if (correct) {
                correctCount++;
            }

            TestRecord record = new TestRecord();
            record.setUserId(userId);
            record.setQuestionType(questionType);
            record.setProject(normalizeProject(project));
            record.setWord(word);
            record.setCorrectTranslation(correctTranslation);
            record.setUserAnswer(StringUtils.hasText(userAnswer) ? userAnswer.trim() : null);
            record.setCorrect(correct);
            record.setCreateTime(LocalDateTime.now());
            records.add(record);

            if (!correct) {
                TestWrongWordResponse wrongWord = new TestWrongWordResponse();
                wrongWord.setQuestionType(questionType);
                wrongWord.setWord(word);
                wrongWord.setUserAnswer(StringUtils.hasText(userAnswer) ? userAnswer.trim() : null);
                wrongWord.setCorrectTranslation(correctTranslation);
                wrongWords.add(wrongWord);
            }
        }

        if (records.isEmpty()) {
            throw new RuntimeException("没有有效的测试数据可以保存");
        }

        this.saveBatch(records);

        TestSubmitResponse response = new TestSubmitResponse();
        response.setProject(normalizeProject(project));
        response.setTotalCount(records.size());
        response.setCorrectCount(correctCount);
        response.setWrongCount(records.size() - correctCount);
        response.setSavedCount(records.size());
        response.setWrongWords(wrongWords);
        response.setRedirectUrl("/test/result");
        return response;
    }

    @Override
    public List<WrongWordBookResponse> getWrongWords(Long userId, String project) {
        LambdaQueryWrapper<TestRecord> wrapper = new LambdaQueryWrapper<TestRecord>()
                .eq(TestRecord::getUserId, userId)
                .eq(TestRecord::getCorrect, false);

        if (StringUtils.hasText(project)) {
            wrapper.eq(TestRecord::getProject, normalizeProject(project));
        }

        wrapper.orderByDesc(TestRecord::getCreateTime);

        List<TestRecord> records = this.list(wrapper);
        List<WrongWordBookResponse> result = new ArrayList<>();

        for (TestRecord record : records) {
            WrongWordBookResponse dto = new WrongWordBookResponse();
            dto.setId(record.getId());
            dto.setQuestionType(record.getQuestionType());
            dto.setProject(record.getProject());
            dto.setWord(record.getWord());
            dto.setCorrectTranslation(record.getCorrectTranslation());
            dto.setUserAnswer(record.getUserAnswer());
            dto.setCreateTime(record.getCreateTime());
            result.add(dto);
        }

        return result;
    }

    @Override
    public List<TestQuestionResponse> generateWrongWordQuestions(Long userId, String project, int count) {
        LambdaQueryWrapper<TestRecord> wrapper = new LambdaQueryWrapper<TestRecord>()
                .eq(TestRecord::getUserId, userId)
                .eq(TestRecord::getCorrect, false);

        if (StringUtils.hasText(project)) {
            wrapper.eq(TestRecord::getProject, normalizeProject(project));
        }

        wrapper.orderByDesc(TestRecord::getCreateTime);

        List<TestRecord> records = this.list(wrapper);

        if (records.isEmpty()) {
            throw new RuntimeException("错题本中没有题目，请先做几次测试");
        }

        // 去重：每个单词只保留最近的一条错题记录
        Map<String, TestRecord> uniqueRecords = new LinkedHashMap<>();
        for (TestRecord record : records) {
            uniqueRecords.putIfAbsent(record.getWord(), record);
        }

        List<TestRecord> distinctRecords = new ArrayList<>(uniqueRecords.values());
        Collections.shuffle(distinctRecords, ThreadLocalRandom.current());

        int targetCount = count > 0 ? Math.min(count, distinctRecords.size()) : distinctRecords.size();
        int choiceCount = Math.max(1, targetCount / 2);
        int fillCount = Math.max(1, targetCount - choiceCount);

        List<TestRecord> choiceItems = distinctRecords.stream().limit(choiceCount).collect(Collectors.toCollection(ArrayList::new));
        List<TestRecord> fillItems = distinctRecords.stream().skip(choiceCount).limit(fillCount).collect(Collectors.toCollection(ArrayList::new));

        // 收集所有错题的翻译作为干扰项
        List<String> allTranslations = distinctRecords.stream()
                .map(TestRecord::getCorrectTranslation)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        List<TestQuestionResponse> questions = new ArrayList<>();

        for (int i = 0; i < choiceItems.size(); i++) {
            TestRecord item = choiceItems.get(i);
            TestQuestionResponse response = new TestQuestionResponse();
            response.setQuestionId("wrong-choice-" + (i + 1));
            response.setQuestionType("choice");
            response.setWord(item.getWord());
            response.setPrompt("选择下列单词的正确翻译：" + item.getWord());
            response.setOptions(buildChoiceOptions(item.getCorrectTranslation(), allTranslations));
            questions.add(response);
        }

        for (int i = 0; i < fillItems.size(); i++) {
            TestRecord item = fillItems.get(i);
            TestQuestionResponse response = new TestQuestionResponse();
            response.setQuestionId("wrong-fill-" + (i + 1));
            response.setQuestionType("fill");
            response.setWord(item.getWord());
            response.setPrompt("根据翻译写出对应单词：" + item.getCorrectTranslation());
            response.setOptions(Collections.emptyList());
            questions.add(response);
        }

        Collections.shuffle(questions, ThreadLocalRandom.current());
        return questions;
    }

    private List<WordItem> loadWordItems(String project) {
        String normalizedProject = normalizeProject(project);
        return switch (normalizedProject) {
            case "CET4" -> cet4Mapper.selectList(new LambdaQueryWrapper<Cet4Word>())
                    .stream()
                    .map(item -> new WordItem(item.getWord(), item.getTranslation()))
                    .filter(item -> StringUtils.hasText(item.word()) && StringUtils.hasText(item.translation()))
                    .collect(Collectors.toCollection(ArrayList::new));
            case "CET6" -> cet6Mapper.selectList(new LambdaQueryWrapper<Cet6Word>())
                    .stream()
                    .map(item -> new WordItem(item.getWord(), item.getTranslation()))
                    .filter(item -> StringUtils.hasText(item.word()) && StringUtils.hasText(item.translation()))
                    .collect(Collectors.toCollection(ArrayList::new));
            default -> throw new RuntimeException("不支持的测试项目");
        };
    }

    private List<String> buildChoiceOptions(String correctTranslation, List<String> allTranslations) {
        List<String> options = new ArrayList<>();
        options.add(correctTranslation);

        List<String> decoys = allTranslations.stream()
                .filter(item -> !item.equals(correctTranslation))
                .collect(Collectors.toCollection(LinkedList::new));
        Collections.shuffle(decoys, ThreadLocalRandom.current());

        for (String decoy : decoys) {
            if (options.size() >= 4) {
                break;
            }
            options.add(decoy);
        }

        Collections.shuffle(options, ThreadLocalRandom.current());
        return options;
    }

    private String resolveTranslation(String project, String word) {
        String normalizedProject = normalizeProject(project);
        return switch (normalizedProject) {
            case "CET4" -> findCet4Translation(word);
            case "CET6" -> findCet6Translation(word);
            default -> throw new RuntimeException("不支持的测试项目");
        };
    }

    private String findCet4Translation(String word) {
        Cet4Word entry = cet4Mapper.selectById(word);
        if (entry == null) {
            throw new RuntimeException("题目不存在：" + word);
        }
        return entry.getTranslation();
    }

    private String findCet6Translation(String word) {
        Cet6Word entry = cet6Mapper.selectById(word);
        if (entry == null) {
            throw new RuntimeException("题目不存在：" + word);
        }
        return entry.getTranslation();
    }

    private boolean isCorrectAnswer(String questionType, String userAnswer, String word, String correctTranslation) {
        if (!StringUtils.hasText(userAnswer) || !StringUtils.hasText(correctTranslation)) {
            return false;
        }

        String normalizedAnswer = normalizeText(userAnswer);
        if ("choice".equals(questionType)) {
            return normalizeText(correctTranslation).equals(normalizedAnswer);
        }

        if ("fill".equals(questionType)) {
            return normalizeText(word).equals(normalizedAnswer);
        }

        String[] options = correctTranslation.split("[;；,，/|、\\s]+");
        for (String option : options) {
            String normalizedOption = normalizeText(option);
            if (!normalizedOption.isEmpty() && (normalizedOption.equals(normalizedAnswer)
                    || normalizedOption.contains(normalizedAnswer)
                    || normalizedAnswer.contains(normalizedOption))) {
                return true;
            }
        }
        return false;
    }

    private String normalizeQuestionType(String questionType) {
        if (!StringUtils.hasText(questionType)) {
            throw new RuntimeException("题目类型不能为空");
        }
        return questionType.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeProject(String project) {
        if (!StringUtils.hasText(project)) {
            throw new RuntimeException("请选择测试项目");
        }
        return project.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String text) {
        return StringUtils.hasText(text) ? text.trim().replaceAll("\\s+", "").toLowerCase(Locale.ROOT) : "";
    }

    private record WordItem(String word, String translation) {
    }
}