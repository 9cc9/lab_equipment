package org.uestc.weglas.controller.support;

import org.uestc.weglas.base.util.BaseResult;
import org.uestc.weglas.base.util.template.AbstractBizCallback;
import org.uestc.weglas.base.util.template.BizTemplate;

import java.util.function.Consumer;

/**
 * Controller 统一走 BizTemplate，且不在各 Controller 内再声明匿名 AbstractBizCallback。
 * <p>
 * 若在 Controller 方法里写 {@code new AbstractBizCallback<>(){...}}，编译器会为每个匿名类生成
 * {@code Outer$1.class、Outer$2.class}，并在访问外层 private 字段时生成 {@code access$0} 等 synthetic 方法。
 * IDEA 增量编译 / 热部署时，外层类已更新而 {@code Outer$N.class} 未重编，运行时就会 NoSuchMethodError。
 * 回调逻辑放在本类固定的 static 内部类中，Controller 侧仅用 lambda + final 局部变量，可避免该问题。
 */
public final class ControllerBizRunner {

    private ControllerBizRunner() {
    }

    public static <T> BaseResult<T> run(Consumer<BaseResult<T>> executor) {
        return BizTemplate.execute(new ResultCallback<>(executor));
    }

    public static <T> BaseResult<T> runWithCheck(Runnable check, Consumer<BaseResult<T>> executor) {
        return BizTemplate.execute(new CheckResultCallback<>(check, executor));
    }

    private static final class ResultCallback<T> extends AbstractBizCallback<T> {
        private final Consumer<BaseResult<T>> executor;

        ResultCallback(Consumer<BaseResult<T>> executor) {
            this.executor = executor;
        }

        @Override
        public void execute(BaseResult<T> result) {
            executor.accept(result);
        }
    }

    private static final class CheckResultCallback<T> extends AbstractBizCallback<T> {
        private final Runnable check;
        private final Consumer<BaseResult<T>> executor;

        CheckResultCallback(Runnable check, Consumer<BaseResult<T>> executor) {
            this.check = check;
            this.executor = executor;
        }

        @Override
        public void checkParameter() {
            check.run();
        }

        @Override
        public void execute(BaseResult<T> result) {
            executor.accept(result);
        }
    }
}
