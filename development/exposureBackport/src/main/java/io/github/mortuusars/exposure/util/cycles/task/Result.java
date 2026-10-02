package io.github.mortuusars.exposure.util.cycles.task;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.util.TranslatableError;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Result<T> {
   @Nullable
   private final T value;
   @Nullable
   private final TranslatableError error;

   private Result(@Nullable("If error is provided") T value, @Nullable("If value is provided") TranslatableError error) {
      Preconditions.checkState(value != null || error != null, "Either value or error must be provided. Both cannot be null.");
      this.value = value;
      this.error = error;
   }

   public boolean isSuccessful() {
      return this.value != null;
   }

   public boolean isError() {
      return this.error != null;
   }

   public Result<T> ifSuccessful(Consumer<T> valueConsumer) {
      if (this.isSuccessful()) {
         valueConsumer.accept(this.getValue());
      }

      return this;
   }

   public Result<T> ifError(Consumer<TranslatableError> errorConsumer) {
      if (this.isError()) {
         errorConsumer.accept(this.getError());
      }

      return this;
   }

   public <R> R map(Function<T, R> ifSuccessful, Function<TranslatableError, R> ifError) {
      return this.isSuccessful() ? ifSuccessful.apply(this.getValue()) : ifError.apply(this.getError());
   }

   @NotNull
   public T getValue() {
      Preconditions.checkState(this.value != null, "Called getValue on an error result. Should check with isSuccessful first.");
      return this.value;
   }

   @NotNull
   public TranslatableError getError() {
      Preconditions.checkState(this.error != null, "Called getError on a successful result. Should check with isError first.");
      return this.error;
   }

   public static <T> Result<T> success(T value) {
      return new Result<>(value, null);
   }

   public static <T> Result<T> error(TranslatableError error) {
      return new Result<>(null, error);
   }

   public T unwrap(Consumer<TranslatableError> errorConsumer) throws IllegalStateException {
      this.ifError(errorConsumer);
      return this.getValue();
   }

   public T unwrap() throws IllegalStateException {
      return this.getValue();
   }

   public <R> Result<R> remapError() {
      return error(this.getError());
   }
}
