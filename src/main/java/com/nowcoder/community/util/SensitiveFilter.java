package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "**";

    //根节点为空
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init(){
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive_words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                ){
            String keyword;
            while((keyword = reader.readLine())!=null){
                //添加到前缀树
                this.addKeyword(keyword);
            }
        }catch (IOException e){
            logger.error("加载敏感词文件失败"+e.getMessage());
        }
    }

    //添加敏感词到前缀树
    private void addKeyword(String keyword){
        TrieNode temp = rootNode;
        for(int i = 0;i < keyword.length();i++){
            char c = keyword.charAt(i);
            TrieNode subNode = temp.getSubNode(c);
            if(subNode == null){
                subNode = new TrieNode();
                temp.addSubNode(c,subNode);
            }
            temp = subNode;

            //设置结束标识
            if(i == keyword.length()-1){
                temp.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String Filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }

        TrieNode tempNode = rootNode;
        int begin = 0;
        int end = 0;
        // sb为返回的过滤后的String
        StringBuilder sb = new StringBuilder();

        while(end < text.length()){
            char c = text.charAt(end);
            //跳过特殊符号
            if(isSymbol(c)){
                if(tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                end++;
                continue;
            }

            //检查是否为敏感词
            tempNode=tempNode.getSubNode(c);
            if(tempNode == null){
                sb.append(text.charAt(begin));
                begin++;
                end = begin;
                tempNode = rootNode;
            }else if(tempNode.isKeywordEnd){
                sb.append(REPLACEMENT);
                begin = end+1;
                end = begin;
                tempNode = rootNode;
            }else{
                end++;
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character c){
        // CharUtils.isAsciiAlphanumeric(c)判断字符是否为普通字符，是则返回true
        // (c < 0x2E80 || c > 0x9FFF)此范围为东亚的文字范围，是正常的文字，需要排除在外
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //前缀树
    private class TrieNode{
        // 关键词结束标识
        private boolean isKeywordEnd = false;

        //检查子节点是否包含
        private Map<Character,TrieNode> subNode = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNode(Character c,TrieNode node){
            subNode.put(c,node);
        }

        public TrieNode getSubNode(Character c){
            return subNode.get(c);
        }
    }

}
