package com.openstudy.notes.mapper;

import com.openstudy.notes.domain.NoteQuestionBank;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoteQuestionBankMapper {
    public NoteQuestionBank selectNoteQuestionBankById(Long id);

    public List<NoteQuestionBank> selectNoteQuestionBankList(NoteQuestionBank noteQuestionBank);

    public int insertNoteQuestionBank(NoteQuestionBank noteQuestionBank);

    public int updateNoteQuestionBank(NoteQuestionBank noteQuestionBank);

    public int deleteNoteQuestionBankById(Long id);

    public int deleteNoteQuestionBankByIds(Long[] ids);

    public List<NoteQuestionBank> selectByNoteId(@Param("noteId") Long noteId);

    public List<NoteQuestionBank> selectByBankId(@Param("bankId") Long bankId);

    public int deleteByNoteIdAndBankId(@Param("noteId") Long noteId, @Param("bankId") Long bankId);

    public int countByNoteId(@Param("noteId") Long noteId);
}
