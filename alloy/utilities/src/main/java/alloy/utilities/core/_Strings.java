package alloy.utilities.core;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import alloy.utilities.core._Exceptions.NotYetImplementedException;
import alloy.utilities.domain.Tuple;
import alloy.utilities.domain.Tuple.Pair;

/**
 * Created by jlutteringer on 1/16/18.
 */
public class _Strings {
	public static List<String> toStrings(Object[] objects) {
		return toStrings(Lists.newArrayList(objects));
	}

	public static List<String> toStrings(List<Object> objects) {
		List<String> strings = Lists.newArrayList();
		for (Object object : objects) {
			if (object != null) {
				strings.add(object.toString());
			}
		}

		return strings;
	}

	public static String toString(byte[] bytes) {
		return new String(bytes, Constants.DEFAULT_ENCODING);
	}

	public static String substituteTokensTest(String source, Map<String, Object> parameters) throws Exception {
		return substituteTokens(source, "${", "}", parameters, true);
	}

	public static String substituteTokens(String source, Map<String, Object> parameters) {
		try {
			return substituteTokens(source, "${", "}", parameters, false);
		} catch (Exception e) {
			// TODO logging
//			log.error(e);
			return source;
		}
	}

	public static String substituteTokens(String source, String tokenPrefix, String tokenSuffix, Map<String, Object> parameters,
	                                      Boolean throwErrorIfParamNotFound) throws Exception{
		List<Pair<StringToken, String>> resolvedTokens = Lists.newArrayList();

		parseTokens(source, tokenPrefix, tokenSuffix).forEach((token) -> {
			String parameterValue = Lists.newArrayList(token.getValue().split("\\.")).stream().findFirst().get();
			if (parameters.containsKey(parameterValue)) {
				Object tokenValue = resolveTokenValue(token.getValue(), parameters.get(parameterValue));
				String stringToken = toString(tokenValue, "null");

				resolvedTokens.add(Tuple.pair(token, stringToken));
			}
			else {
				if (throwErrorIfParamNotFound) {
					try {
						throw new Exception("Param " + parameterValue + " not found in source " + source);
					} catch (Exception e) {
						// TODO logging
//						log.error(e);
					}
				}
				// TODO logging
//				logger.debug("Unable to resolve value [{}] because value is not in parameter list {}", parameterValue, parameters.keySet());
			}
		});

		return applyResolvedTokens(source, resolvedTokens);
	}

	private static String applyResolvedTokens(String source, List<Pair<StringToken, String>> resolvedTokens) {
		StringBuilder builder = new StringBuilder();
		int currentIndex = 0;
		for (Pair<StringToken, String> resolvedToken : resolvedTokens) {
			StringToken token = resolvedToken.getFirst();
			builder.append(source.substring(currentIndex, token.getIndex()));
			currentIndex = resolvedToken.getFirst().getIndex() + token.getLength();
			builder.append(resolvedToken.getSecond());
		}

		builder.append(source.substring(currentIndex, source.length()));
		return builder.toString();
	}

	private static String toString(Object tokenValue, String defaultValue) {
		if (tokenValue == null) {
			return defaultValue;
		}
		return tokenValue.toString();
	}

	private static Object resolveTokenValue(String value, Object targetObject) {
		List<String> pathParts = Lists.newArrayList(value.split("\\.")).stream().skip(1).collect(Collectors.toList());
		for (String part : pathParts) {
			if (targetObject == null) {
				break;
			}

			for (String methodName : getMethodNames(part)) {
				Method method = ReflectionUtils.findMethod(targetObject.getClass(), methodName, new Class<?>[0]);
				if (method != null) {
					targetObject = ReflectionUtils.invokeMethod(method, targetObject);
					break;
				}
			}
		}
		return targetObject;
	}

	private static List<String> getMethodNames(String name) {
		String camelCaseName = convert(name, StringFormatType.CAMEL_CASE);
		return Lists.newArrayList(
				"get" + StringUtils.capitalize(camelCaseName),
				"is" + StringUtils.capitalize(camelCaseName));
	}

	private static Stream<StringToken> parseTokens(String source, String tokenPrefix, String tokenSuffix) {
		return StreamSupport.stream(new StringTokenIterable(source, tokenPrefix, tokenSuffix).spliterator(), false);
	}

	private static Stream<String> getTokensBetween(String source, String tokenPrefix, String tokenSuffix, final int beginIndex, final int endIndex, final boolean inclusive) {
		// Iterate through tokens
		Stream<StringToken> tokenParser = parseTokens(source, tokenPrefix, tokenSuffix);

		// Filter out the beginning case
		tokenParser = tokenParser.filter(input -> {
			if (beginIndex > input.getIndex()) {
				if ((beginIndex < input.getIndex() + input.getValue().length()) && inclusive) {
					return true;
				}
				else {
					return false;
				}
			}
			else {
				return true;
			}
		});

		// Filter out the end case
		tokenParser = tokenParser.filter(input -> {
			if (endIndex < input.getIndex() + input.getValue().length()) {
				if (endIndex > input.getIndex() && inclusive) {
					return true;
				}
				else {
					return false;
				}
			}
			else {
				return true;
			}
		});

		// Convert to strings
		return tokenParser.map(StringToken::getValue);
	}

	public static Optional<Pair<String,String>> splitPair(String target, String separator) {
		if(target == null || separator == null) {
			return Optional.empty();
		}

		String[] result = StringUtils.splitByWholeSeparator(target, separator, 2);

		if(result.length == 2) {
			return Optional.of(Tuple.pair(result[0], result[1]));
		}
		else {
			return Optional.empty();
		}
	}

	public enum StringFormatType {
		PRETTY,
		CAMEL_CASE,
		CAPITAL_CAMEL_CASE,
		CAPITAL_UNDERSCORE,
		LOWERCASE_UNDERSCORE,
		MIXED_UNDERSCORE,
		LOWERCASE_DASH
	}

	private final static Pattern HAS_UPPERCASE = Pattern.compile("^.*[A-Z]+.*$");
	private final static Pattern HAS_LOWERCASE = Pattern.compile("^.*[a-z]+.*$");

	public static String smartConvert(String string, StringFormatType toType) {
		StringFormatType fromType = getStringType(string);
		return convert(string, fromType, toType);
	}

	public static boolean smartEquals(String one, String two) {
		try {
			StringFormatType typeOne = getStringType(one);
			StringFormatType typeTwo = getStringType(two);

			String oneNormalized = convert(one, typeOne, StringFormatType.CAPITAL_UNDERSCORE);
			String twoNormalized = convert(two, typeTwo, StringFormatType.CAPITAL_UNDERSCORE);

			return Objects.equal(oneNormalized, twoNormalized);
		} catch (Exception e) {
			return false;
		}
	}

	private static RuntimeException getUnknownFormatException(String string) {
		return new RuntimeException("Cannot determine format type for string " + string);
	}

	private static StringFormatType getStringType(String string) {
		if (hasDelimiters(string)) {
			if (hasSpaces(string)) {
				return StringFormatType.PRETTY;
			}

			if (hasDashes(string)) {
				return StringFormatType.LOWERCASE_DASH;
			}

			if (hasUnderscores(string)) {
				if (isUppercase(string)) {
					return StringFormatType.CAPITAL_UNDERSCORE;
				}
				else if (isLowercase(string)) {
					return StringFormatType.LOWERCASE_UNDERSCORE;
				}
				else {
					return StringFormatType.MIXED_UNDERSCORE;
				}
			}

			if (isCapitalized(string)) {
				return StringFormatType.CAPITAL_CAMEL_CASE;
			}
			else {
				return StringFormatType.CAMEL_CASE;
			}
		}
		else {
			if (isUppercase(string)) {
				return StringFormatType.CAPITAL_UNDERSCORE;
			}
			if (isCapitalized(string)) {
				return StringFormatType.CAPITAL_CAMEL_CASE;
			}
			if (isLowercase(string)) {
				return StringFormatType.CAMEL_CASE;
			}

			throw getUnknownFormatException(string);
		}
	}

	private static boolean hasDelimiters(String string) {
		if (hasSpaces(string)) {
			return true;
		}
		if (hasUnderscores(string)) {
			return true;
		}
		if (hasDashes(string)) {
			return true;
		}

		return string.length() > 1 && isMixedCase(string.substring(1));
	}

	private static boolean hasDashes(String string) {
		return string.contains("-");
	}

	private static boolean isMixedCase(String string) {
		return hasUppercase(string) && hasLowercase(string);
	}

	private static boolean hasLowercase(String string) {
		return HAS_LOWERCASE.matcher(string).matches();
	}

	private static boolean hasUppercase(String string) {
		return HAS_UPPERCASE.matcher(string).matches();
	}

	private static boolean isUppercase(String string) {
		return StringUtils.isAllUpperCase(removeDelimiters(string));
	}

	private static boolean isLowercase(String string) {
		return StringUtils.isAllLowerCase(removeDelimiters(string));
	}

	private static String removeDelimiters(String string) {
		return StringUtils.remove(StringUtils.remove(StringUtils.remove(string, "_"), "-"), " ");
	}

	private static boolean hasUnderscores(String string) {
		return string.contains("_");
	}

	private static boolean hasSpaces(String string) {
		return string.contains(" ");
	}

	private static boolean isCapitalized(String string) {
		return Character.isUpperCase(string.charAt(0));
	}

	public static String convert(String string, StringFormatType toType) {
		if (StringUtils.isBlank(string)) {
			return string;
		}

		StringFormatType fromType = getStringType(string);
		return convert(string, fromType, toType);
	}

	public static String convert(String string, StringFormatType fromType, StringFormatType toType) {
		if (StringUtils.isBlank(string)) {
			return string;
		}

		List<String> words = tokenize(string, fromType);
		List<String> transformedWords = Lists.newArrayList();
		int count = 0;
		for (String word : words) {
			String transformedWord = word.toLowerCase();
			transformedWord = format(transformedWord, toType, count);
			transformedWords.add(transformedWord);
			count++;
		}

		return combine(transformedWords, toType);
	}

	private static List<String> tokenize(String string, StringFormatType fromType) {
		if (fromType.equals(StringFormatType.CAPITAL_UNDERSCORE) || fromType.equals(StringFormatType.LOWERCASE_UNDERSCORE) || fromType.equals(StringFormatType.MIXED_UNDERSCORE)) {
			return Arrays.asList(string.split("_"));
		}
		else if (fromType.equals(StringFormatType.LOWERCASE_DASH)) {
			return Arrays.asList(string.split("-"));
		}
		else if (fromType.equals(StringFormatType.CAMEL_CASE) || fromType.equals(StringFormatType.CAPITAL_CAMEL_CASE)) {
			return Arrays.asList(string.split("(?<!(^|\\p{Lu}))(?=\\p{Lu})|(?<!^)(?=\\p{Lu}\\p{Ll})"));
		}
		else if (fromType.equals(StringFormatType.PRETTY)) {
			return Arrays.asList(string.split("\\W+"));
		}

		throw new NotYetImplementedException();
	}

	private static String format(String word, StringFormatType toType, int index) {
		if (toType.equals(StringFormatType.PRETTY)) {
			return StringUtils.capitalize(word);
		}
		if (toType.equals(StringFormatType.LOWERCASE_UNDERSCORE) || toType.equals(StringFormatType.LOWERCASE_DASH)) {
			return word;
		}
		if (toType.equals(StringFormatType.CAMEL_CASE)) {
			if (index == 0) {
				return word.toLowerCase();
			}
			else {
				return StringUtils.capitalize(word);
			}
		}
		if (toType.equals(StringFormatType.CAPITAL_CAMEL_CASE)) {
			return StringUtils.capitalize(word);
		}
		if (toType.equals(StringFormatType.CAPITAL_UNDERSCORE)) {
			return word.toUpperCase();
		}

		throw new NotYetImplementedException();
	}

	private static String combine(List<String> words, StringFormatType toType) {
		if (toType.equals(StringFormatType.PRETTY)) {
			return StringUtils.join(words, " ");
		}
		if (toType.equals(StringFormatType.LOWERCASE_UNDERSCORE) || toType.equals(StringFormatType.CAPITAL_UNDERSCORE)) {
			return StringUtils.join(words, "_");
		}
		if (toType.equals(StringFormatType.LOWERCASE_DASH)) {
			return StringUtils.join(words, "-");
		}
		if (toType.equals(StringFormatType.CAMEL_CASE) || toType.equals(StringFormatType.CAPITAL_CAMEL_CASE)) {
			return StringUtils.join(words, "");
		}

		throw new NotYetImplementedException();
	}

	public static class StringToken implements Serializable {
		private static final long serialVersionUID = -8252126400328088545L;

		private final String value;
		private final int index;
		private final int length;

		public StringToken(String value, int index, int length) {
			super();
			this.value = value;
			this.index = index;
			this.length = length;
		}

		public String getValue() {
			return value;
		}

		public int getIndex() {
			return index;
		}

		public int getLength() {
			return length;
		}
	}

	public static class StringTokenIterator extends SingleEntryIterator<StringToken> {
		private final String tokenPrefix;
		private final String tokenSuffix;
		private final String source;

		private int currentLocation = 0;

		public StringTokenIterator(String source, String tokenPrefix, String tokenSuffix) {
			Preconditions.checkNotNull(tokenPrefix);
			Preconditions.checkNotNull(tokenSuffix);
			this.source = StringUtils.defaultString(source);
			this.tokenPrefix = tokenPrefix;
			this.tokenSuffix = tokenSuffix;
		}

		@Override
		protected StringToken generateNext() throws NoSuchElementException {
			if (currentLocation >= source.length() - 1) {
				throw new NoSuchElementException();
			}

			int tokenPrefixIndex = source.indexOf(tokenPrefix, currentLocation);
			if (tokenPrefixIndex == -1) {
				throw new NoSuchElementException();
			}

			currentLocation = tokenPrefixIndex + tokenPrefix.length();

			int tokenSuffixIndex = source.indexOf(tokenSuffix, currentLocation);
			if (tokenSuffixIndex == -1) {
				throw new NoSuchElementException();
			}

			// If we don't want tokens to overlap, we should add the token suffix to the current location as well
			// Right now we default to overlap but that's probably bad - we default to it tho because we're using
			// This for words, which do have placeholder overlap
			// Perhaps this should be an option?
			currentLocation = tokenSuffixIndex;

			String value = source.substring(tokenPrefixIndex + tokenPrefix.length(), tokenSuffixIndex);
			return new StringToken(value, tokenPrefixIndex, tokenPrefix.length() + value.length() + tokenSuffix.length());
		}
	}

	public static class StringTokenIterable implements Iterable<StringToken> {
		private final String tokenPrefix;
		private final String tokenSuffix;
		private final String source;

		public StringTokenIterable(String source, String tokenPrefix, String tokenSuffix) {
			this.tokenPrefix = tokenPrefix;
			this.tokenSuffix = tokenSuffix;
			this.source = source;
		}

		@Override
		public Iterator<StringToken> iterator() {
			return new StringTokenIterator(source, tokenPrefix, tokenSuffix);
		}
	}
}