package com.openstudy.notes.service;

import com.openstudy.notes.domain.NoteQuestionBank;

import java.util.List;

public interface INoteQuestionBankService {
    public NoteQuestionBank selectNoteQuestionBankById(Long id);

    public List<NoteQuestionBank> selectNoteQuestionBankList(NoteQuestionBank noteQuestionBank);

    public int insertNoteQuestionBank(NoteQuestionBank noteQuestionBank);

    public int updateNoteQuestionBank(NoteQuestionBank noteQuestionBank);

    public int deleteNoteQuestionBankById(Long id);

    public int deleteNoteQuestionBankByIds(Long[] ids);

    public List<NoteQuestionBank> selectByNoteId(Long noteId);

    public List<NoteQuestionBank> selectByBankId(Long bankId);

    public int deleteByNoteIdAndBankId(Long noteId, Long bankId);

    public int countByNoteId(Long noteId);
}
